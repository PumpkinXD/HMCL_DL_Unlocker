# Third Party Notices

`HMCL_DL_Unlocker.jar` is shipped as a shaded (uber) JAR and therefore bundles the third-party components listed below. Their license texts are reproduced verbatim under [`LICENSES/`](LICENSES/).

| Component | Coordinate | Version | License | Full text |
| --- | --- | --- | --- | --- |
| Byte Buddy | `net.bytebuddy:byte-buddy` | 1.18.15 | Apache License, Version 2.0 | [`LICENSES/byte-buddy/LICENSE`](LICENSES/byte-buddy/LICENSE) |
| ASM *(bundled inside Byte Buddy)* | — | 9.11 | 3-clause BSD | [`LICENSES/ASM/LICENSE`](LICENSES/ASM/LICENSE) |

---

## 1. Byte Buddy

- **Group:** `net.bytebuddy`
- **Name:** `byte-buddy`
- **Version:** `1.18.15`
- **License:** Apache License, Version 2.0 — <https://www.apache.org/licenses/LICENSE-2.0.txt>
- **Upstream:** <https://github.com/raphw/byte-buddy>
- **Bundled files:**
  - [`LICENSES/byte-buddy/LICENSE`](LICENSES/byte-buddy/LICENSE) — from `META-INF/LICENSE`
  - [`LICENSES/byte-buddy/NOTICE`](LICENSES/byte-buddy/NOTICE) — from `META-INF/NOTICE`

The original NOTICE file reads:

```
Copyright 2014 - Present Rafael Winterhalter

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Per its own manifest, this artifact is *"a build of Byte Buddy with all ASM dependencies repackaged into its own name space"*. ASM's license is therefore reproduced below.

---

## 2. ASM

- **Name:** ASM
- **Version:** 9.11
- **Bundled in:** `net.bytebuddy:byte-buddy:1.18.15`
- **License:** 3-clause BSD
- **Upstream:** <https://asm.ow2.io>
- **Bundled file:** [`LICENSES/ASM/LICENSE`](LICENSES/ASM/LICENSE) — from `META-INF/licenses/ASM`

```
ASM: a very small and fast Java bytecode manipulation framework
Copyright (c) 2000-2011 INRIA, France Telecom
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holders nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.
```

The full text, including the warranty disclaimer, is in [`LICENSES/ASM/LICENSE`](LICENSES/ASM/LICENSE).

---

## Attribution note

The `META-INF/LICENSE`, `META-INF/NOTICE` and `META-INF/licenses/ASM` entries present inside the distributed JAR belong to the third-party components above. **They do not describe the license of this project itself.**

`HMCL_DL_Unlocker` is licensed under the GNU General Public License v3.0 — see [`LICENSE`](LICENSE).

Byte Buddy's packages are relocated to `io.github.pumpkinxd.hmcl.dlunlocker.shaded.net.bytebuddy` by the Shadow Gradle plugin. Relocation does not alter any of the license obligations above.

`HMCL_DL_Unlocker` neither modifies, embeds, nor redistributes HMCL itself. HMCL is a separate GPL-3.0 work by its own authors: <https://github.com/HMCL-dev/HMCL>
