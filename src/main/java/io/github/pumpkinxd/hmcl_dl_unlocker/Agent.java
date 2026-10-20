package io.github.pumpkinxd.hmcl_dl_unlocker;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Agent {
    static final String TARGET_TYPE   = "org.jackhuang.hmcl.util.RegionUtils";
    static final String TARGET_METHOD = "isIsChinaMainlandNetwork";

    public static void premain(String arg, Instrumentation inst) { install(inst); }
    public static void agentmain(String arg, Instrumentation inst) { install(inst); }

    private static void install(Instrumentation inst) {
        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.DISABLED)   // 不做 retransform
                .with(new AgentBuilder.Listener.Adapter() {
                    @Override public void onComplete(String typeName, ClassLoader cl, JavaModule m, boolean loaded) {
                        if (TARGET_TYPE.equals(typeName))
                            System.out.println("[hmcl-patch] OK: patched " + typeName);
                    }
                    @Override public void onError(String typeName, ClassLoader cl, JavaModule m, boolean loaded, Throwable t) {
                        System.err.println("[hmcl-patch] ERROR on " + typeName + ": " + t);
                    }
                })
                .type(named(TARGET_TYPE))
                .transform((b, type, cl, module, pd) -> b
                        .method(named(TARGET_METHOD)
                                .and(takesArguments(0))
                                .and(returns(boolean.class)))
                        .intercept(net.bytebuddy.implementation.FixedValue.value(false)))
                .installOn(inst);
    }
}
