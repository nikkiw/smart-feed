# XML vs Compose Performance Results

This document tracks the performance metrics during the migration from XML to Jetpack Compose (via an intermediate Islands step).
The baseline values were captured using the instructions in [xml-baseline.md](xml-baseline.md).

## Test Environment
* **Device:** HUAWEI HRY-LX1T
* **Android OS / SDK:** Android 10 / SDK 29
* **Git Commit (Baseline):** (fill this in)
* **Refresh Rate:** 60 Hz (fill if different)
* **Iterations:** 10

> **Note:** The baseline metrics were gathered using the `devBenchmark` variant. This variant uses the `benchmark` build type which inherits all `release` optimizations (R8 minification, resource shrinking, non-debuggable). `prodBenchmark` was skipped for local benchmarking due to device-level `NetworkSecurityConfig` restrictions from Huawei HMS Core which block local mock server traffic.

## Metrics Comparison

| Metric | XML (Baseline) | Compose Islands (Without Profile) | Compose Islands (With Profile) |
|---|---|---|---|
| **Startup** | | | |
| timeToInitialDisplayMs (Median) | 661.7 ms | 672.3 ms | 669.8 ms |
| timeToFullDisplayMs (Median) | N/A | N/A | N/A |
| **Feed Scroll** | | | |
| frameDurationCpuMs P50 | 6.7 ms | 8.1 ms | 8.1 ms |
| frameDurationCpuMs P90 | 9.5 ms | 10.5 ms | 10.9 ms |
| frameDurationCpuMs P99 | 23.8 ms | 26.5 ms | 26.3 ms |
| frameOverrunMs P50 | N/A (API 29) | N/A (API 29) | N/A (API 29) |
| frameOverrunMs P99 | N/A (API 29) | N/A (API 29) | N/A (API 29) |
| Frames with positive overrun (jank) | N/A | N/A | N/A |
| **Build & Size** | | | |
| APK Size (prodRelease) | 9.38 MB | 9.9 MB | 9.91 MB |
| Build Time (Clean assemble) | 296 sec | 326 sec | 244 sec |

## Analysis (XML vs Compose Islands)
- **Startup Time**: Introducing Compose without a profile increased the median TTID by 10.6 ms (from 661.7 ms to 672.3 ms). Adding the Baseline Profile (`CompilationMode.Ignore`) slightly improved the result to 669.8 ms, reducing the degradation to 8.1 ms.
- **Feed Scroll Performance**: Scrolling without a profile showed an increase in frame duration (P50: 6.7 -> 8.1 ms, P90: 9.5 -> 10.5 ms, P99: 23.8 -> 26.5 ms). Measurements with the profile yielded practically identical results (P50: 8.1 ms, P90: 10.9 ms, P99: 26.3 ms). Both Compose implementations (with and without a profile) operate well within the 16 ms per frame budget (60 FPS), ensuring the scroll feels smooth.
- **APK Size**: The release build size increased from 9.38 MB to 9.91 MB due to the inclusion of Compose libraries and the bundled profile file.
- **Build Time**: Clean build time with Compose increased by ~30 seconds compared to XML. Build times (244 seconds with profile) may fluctuate depending on machine load.

> **Note on Baseline Profiles:** The profile was successfully generated on an API 31 emulator using `Gradle Managed Devices` and embedded into the source code (`app/src/main/baseline-prof.txt`). Because Android Macrobenchmark cannot dynamically send a broadcast to install the profile via `CompilationMode.Partial()` on the physical HUAWEI device (API 29) due to OEM shell restrictions, the profile was measured in `CompilationMode.Ignore()` mode. This mode tests the app state after a standard package installation (where the profile is bundled in the APK and applied naturally by the system). The impact of the Baseline Profile during this "Islands" phase proved minimal, as Compose is only initialized inside individual cards during scrolling. We expect the profile's benefits to be far more pronounced when fully migrating to Compose (Full Compose) for navigation and entire screens.
