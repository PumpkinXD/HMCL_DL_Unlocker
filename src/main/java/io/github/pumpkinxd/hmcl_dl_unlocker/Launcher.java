package io.github.pumpkinxd.hmcl_dl_unlocker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Launcher {

    private static final String HMCL_MAIN_CLASS = "org.jackhuang.hmcl.Main";
    private static final int MIN_JAVA_VERSION = 17;
    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");

    private Launcher() {
    }

    public static void main(String[] args) throws Exception {
        boolean dryRun = args.length > 0 && Arrays.asList(args).contains("--dry-run");
        Path self = selfPath();

        // 目标：优先命令行参数，其次与补丁 jar 同目录自动探测
        Path target = null;
        if (args.length > 0 && !args[0].startsWith("-")) {
            target = Paths.get(args[0]).toAbsolutePath().normalize();
        } else {
            target = findHmcl(self);
        }

        if (target == null) {
            System.err.println("[hmcl-patch] 未找到 HMCL。");
            System.err.println("[hmcl-patch] 请把 hmcl-patch.jar 放到 HMCL.jar / HMCL.exe 同一个目录下，或：");
            System.err.println("[hmcl-patch]   java -jar hmcl-patch.jar <HMCL.jar 或 HMCL.exe 的路径>");
            System.exit(2);
        }

        Path java = findJava();
        if (java == null) {
            System.err.println("[hmcl-patch] 未找到 Java " + MIN_JAVA_VERSION + " 及以上，请安装后重试。");
            System.exit(3);
        }

        boolean isExe = !target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar");

        List<String> command = new ArrayList<>();
        command.add(java.toAbsolutePath().toString());
        command.add("-javaagent:" + self.toAbsolutePath());
        command.addAll(extraVmOptions());
        command.add("-jar");
        command.add(target.toAbsolutePath().toString());
        command.addAll(forwardArgs(args));

        System.out.println("[hmcl-patch] 补丁: " + self.getFileName());
        System.out.println("[hmcl-patch] 目标: " + target.getFileName()
                + (isExe ? "  (exe 内部即 jar，直接用 java 启动)" : ""));
        System.out.println("[hmcl-patch] Java: " + java);
        System.out.println("[hmcl-patch] 命令: " + String.join(" ", command));

        if (dryRun) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        Path parent = target.getParent();
        if (parent != null) {
            pb.directory(parent.toFile());
        }
        pb.inheritIO();
        System.exit(pb.start().waitFor());
    }

    // ---------- 目标探测 ----------

    /**
     * 依次搜索：补丁 jar 所在目录 → 当前工作目录 → 用户主目录。jar 优先于 exe。
     */
    private static Path findHmcl(Path self) {
        List<Path> searchDirs = new ArrayList<>();
        Path parent = self.toAbsolutePath().getParent();
        if (parent != null) {
            searchDirs.add(parent);
        }
        searchDirs.add(Paths.get("").toAbsolutePath());
        searchDirs.add(Paths.get(System.getProperty("user.home", ".")));

        final List<Path> jars = new ArrayList<>();
        final List<Path> executables = new ArrayList<>();

        for (Path dir : searchDirs) {
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (var stream = Files.list(dir)) {
                stream.filter(Files::isRegularFile).forEach(p -> {
                    String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                    if (!name.contains("hmcl")) {
                        return;
                    }
                    if (name.endsWith(".jar")) {
                        jars.add(p);
                    } else if (name.endsWith(".exe") || name.endsWith(".sh")) {
                        executables.add(p);
                    }
                });
            } catch (IOException ignored) {
                // 目录不可读，跳过
            }
        }

        jars.sort(Comparator.comparing(Path::toString));
        executables.sort(Comparator.comparing(Path::toString));

        for (Path p : jars) {
            if (isHmclArchive(p)) return p;
        }
        for (Path p : executables) {
            if (isHmclArchive(p)) return p;
        }
        return null;
    }

    /**
     * 校验是否是 HMCL：读 manifest 里的 Main-Class。
     * java.util.zip.ZipFile 能容忍文件头部被塞入 exe stub（这正是官方 exe 的结构）。
     */
    private static boolean isHmclArchive(Path file) {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            ZipEntry entry = zip.getEntry("META-INF/MANIFEST.MF");
            if (entry == null) {
                return false;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                String manifest = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return manifest.contains("Main-Class: " + HMCL_MAIN_CLASS);
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    // ---------- Java 探测 ----------

    private static Path findJava() {
        return ProcessHandle.current()
                .info()
                .command()
                .map(Paths::get)
                .orElseGet(() -> Paths.get(System.getProperty("java.home"), "bin",
                        WINDOWS ? "java.exe" : "java"));
    }
    // ---------- 杂项 ----------

    /**
     * 附加 JVM 参数，用于内存设置等。变量名沿用 HMCL 官方 run 任务里的 HMCL_JAVA_OPTS。
     */
    private static List<String> extraVmOptions() {
        String raw = System.getenv("HMCL_PATCH_JAVA_OPTS");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("HMCL_JAVA_OPTS");
        }
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.trim().split("\\s+")).filter(s -> !s.isEmpty()).toList();
    }

    /**
     * 跳过第一个位置参数（HMCL 路径）与 --dry-run，其余原样透传给 HMCL。
     */
    private static List<String> forwardArgs(String[] args) {
        List<String> result = new ArrayList<>();
        for (int i = (args.length > 0 && !args[0].startsWith("-")) ? 1 : 0; i < args.length; i++) {
            if (!"--dry-run".equals(args[i])) {
                result.add(args[i]);
            }
        }
        return result;
    }

    private static Path selfPath() throws Exception {
        return Paths.get(Launcher.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
    }
}
