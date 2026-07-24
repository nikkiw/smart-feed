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

| Metric | XML (Baseline) | Compose Islands | Full Compose |
|---|---|---|---|
| **Startup** | | | |
| timeToInitialDisplayMs (Median) | 661.7 ms | - ms | - ms |
| timeToFullDisplayMs (Median) | N/A | - ms | - ms |
| **Feed Scroll** | | | |
| frameDurationCpuMs P50 | 6.7 ms | - ms | - ms |
| frameDurationCpuMs P90 | 9.5 ms | - ms | - ms |
| frameDurationCpuMs P99 | 23.8 ms | - ms | - ms |
| frameOverrunMs P50 | N/A (API 29) | - ms | - ms |
| frameOverrunMs P99 | N/A (API 29) | - ms | - ms |
| Frames with positive overrun (jank) | N/A | - frames | - frames |
| **Build & Size** | | | |
| APK Size (prodRelease) | 9.38 MB | - MB | - MB |
| Build Time (Clean assemble) | 296 sec | - sec | - sec |

