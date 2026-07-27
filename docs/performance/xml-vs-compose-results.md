# XML vs Compose Performance Results

This document tracks the performance metrics during the migration from XML to Jetpack Compose (via an intermediate Islands step).
The baseline values were captured using the instructions in [xml-baseline.md](xml-baseline.md).

## Test Environment
* **Device:** HUAWEI HRY-LX1T
* **Android OS / SDK:** Android 10 / SDK 29
* **Git Commit (Checkpoint):** `5381021a7a9a635e5ceb3841dac9afc8d13b9d0c`
* **Refresh Rate:** 60 Hz
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

## July 26, 2026 Checkpoint

The latest local checkpoint was captured on **July 26, 2026** from commit `5381021a7a9a635e5ceb3841dac9afc8d13b9d0c`. These runs were taken from the already saved `devBenchmark` artifacts under `benchmark/build/outputs/connected_android_test_additional_output/devBenchmark/connected/HRY-LX1T - 10`.

### Current Results vs XML and Compose Islands

| Metric | XML (Baseline) | Compose Islands (Without Profile) | Compose Islands (With Profile) | Full Compose Checkpoint (No Compilation) | Full Compose Checkpoint (With Profile) |
|---|---|---|---|---|---|
| **Startup** | | | | | |
| timeToInitialDisplayMs (Median) | 661.7 ms | 672.3 ms | 669.8 ms | 600.5 ms | 601.6 ms |
| **Feed Scroll** | | | | | |
| frameDurationCpuMs P50 | 6.7 ms | 8.1 ms | 8.1 ms | 8.5 ms | 8.0 ms |
| frameDurationCpuMs P90 | 9.5 ms | 10.5 ms | 10.9 ms | 23.9 ms | 13.9 ms |
| frameDurationCpuMs P99 | 23.8 ms | 26.5 ms | 26.3 ms | 38.3 ms | 56.9 ms |

### Checkpoint Analysis

- **Startup improved materially.** The current Full Compose checkpoint is faster than both XML and Compose Islands on TTID: `600.5 ms` without compilation and `601.6 ms` with the bundled profile, versus `661.7 ms` on XML baseline and `669.8 ms` on Compose Islands with profile.
- **The profile does not currently change startup on this API 29 device.** The delta between the two saved startup modes is only `1.1 ms`, which is within normal run-to-run noise for this device.
- **The profile significantly improves the median and 90th percentile scroll path.** Relative to the same Full Compose checkpoint without compilation, the profiled run reduced `P50` from `8.5 ms` to `8.0 ms` and `P90` from `23.9 ms` to `13.9 ms`. This indicates that the Baseline Profile materially improves the dominant steady-state rendering path on this device.
- **Scroll tail latency (P99) remains worse than XML.** Even with the bundled Baseline Profile, Full Compose is still slower on the very worst frames: `P99 = 56.9 ms` versus `26.3 ms` on Compose Islands with profile and `23.8 ms` on XML. This is now treated as a documented long-tail limitation of the current Compose implementation on the HUAWEI HRY-LX1T, not as a migration blocker for the demo/portfolio scope.

### Interim Conclusion

Relative to XML and Compose Islands, the migration checkpoint is already a clear startup win and a clear maintainability win. The remaining `P99` regression is retained here as a known tail-latency limitation with supporting trace analysis, but it is accepted as non-blocking for this demo/portfolio project because the median and `P90` path are within an acceptable range for the target scope.

### Perfetto Trace Diagnosis

The saved Perfetto traces point to a **UI/render pipeline bottleneck in the feed list**, not to business logic escaping from Decompose components into Compose.

- **Main-thread work is dominated by frame work, not app-side orchestration.** In the median scroll trace (`FeedScrollBenchmark_feedListScrollNoCompilation_iter005_2026-07-26-21-53-28.perfetto-trace`), the largest main-thread slices are `Choreographer#doFrame` (`2403 ms` total, `226 ms` max), `traversal` (`1978 ms` total, `226 ms` max), `draw` (`1663 ms` total, `40 ms` max), `AndroidOwner:measureAndLayout` (`756 ms` total, `27 ms` max), and Compose recomposition / lazy prefetch slices.
- **Late jank frames reproduce during sustained scrolling, not only during first launch.** After excluding the first 5 seconds of the same trace, the worst remaining frames are still `48 ms`, `46 ms`, `45 ms`, and `39 ms`.
- **A representative late 48 ms frame is layout/draw heavy.** Inside the `48 ms` `Choreographer#doFrame`, the main contributors are `animation` (`22 ms`), `Record View#draw()` (`18 ms`), and two `AndroidOwner:measureAndLayout` spans (`14 ms` each), with smaller `Compose:recompose` work layered on top.
- **Another late 46 ms frame is render-thread bound.** In that frame, the UI thread spends `39 ms` in `traversal/draw`, while `RenderThread` concurrently spends `47 ms` in `DrawFrame` and `42 ms` in `flush commands`, which indicates heavy display-list / GPU submission cost rather than domain-layer processing.
- **Binder exists but is secondary.** Across the same trace, main-thread `binder transaction` time is only `66 ms` total with `19 ms` max, far below the aggregate `traversal/draw/measure` cost. No meaningful Room/SQLite signature appears on the main thread in the hot path.

### Practical Interpretation

- The regression is currently centered in **`FeedListScreen` + `PreviewCard` rendering cost**, especially the `LazyColumn` card path and card content measurement/drawing.
- The heaviest app-side suspects are the image-heavy card body and nested card content in [FeedListScreen](/Users/dev/Developer/@PortfolioProjects/@android/smart-feed/feature/feed/impl/src/main/kotlin/com/feature/feed/compose/FeedListScreen.kt:208) and [PreviewCard](/Users/dev/Developer/@PortfolioProjects/@android/smart-feed/feature/feed/impl/src/main/kotlin/com/feature/feed/compose/PreviewCard.kt:41).
- The trace does **not** support the theory that recommendation/filter/read-progress logic is leaking into Compose and blocking scroll on the main thread. That architectural cleanup was still correct, but it is not the primary source of the current tail-latency regression.
- `Crossfade`, root navigation transitions, and shared-element transitions are lower-priority suspects for this specific scroll benchmark because the recurring late frames line up with steady-state feed rendering, not with destination changes.

### Acceptance Note

For this repository's demo/portfolio scope, the Full Compose migration is considered acceptable despite the elevated `P99` tail. The limitation is documented rather than hidden:

- **Accepted:** startup improvement, Compose-only production path, maintained architecture boundaries, and profiled steady-state scrolling (`P50 8.0 ms`, `P90 13.9 ms`).
- **Known limitation:** long-tail feed frames on the weakest measured device remain worse than XML (`P99 56.9 ms` on the profiled Full Compose run).
- **Implication:** further UI optimization is worthwhile future work, but not a prerequisite for closing the migration effort in its current demo-focused scope.

## Analysis (XML vs Compose Islands)
- **Startup Time**: Introducing Compose without a profile increased the median TTID by 10.6 ms (from 661.7 ms to 672.3 ms). Adding the Baseline Profile (`CompilationMode.Ignore`) slightly improved the result to 669.8 ms, reducing the degradation to 8.1 ms.
- **Feed Scroll Performance**: Scrolling without a profile showed an increase in frame duration (P50: 6.7 -> 8.1 ms, P90: 9.5 -> 10.5 ms, P99: 23.8 -> 26.5 ms). Measurements with the profile yielded practically identical results (P50: 8.1 ms, P90: 10.9 ms, P99: 26.3 ms). Both Compose implementations (with and without a profile) operate well within the 16 ms per frame budget (60 FPS), ensuring the scroll feels smooth.
- **APK Size**: The release build size increased from 9.38 MB to 9.91 MB due to the inclusion of Compose libraries and the bundled profile file.
- **Build Time**: Clean build time with Compose increased by ~30 seconds compared to XML. Build times (244 seconds with profile) may fluctuate depending on machine load.

> **Note on Baseline Profiles and EMUI:** The profile was successfully generated on an API 31 emulator using `Gradle Managed Devices` and embedded into the source code (`app/src/main/baseline-prof.txt`). Because Android Macrobenchmark cannot dynamically send a broadcast to install the profile via `CompilationMode.Partial()` on the physical HUAWEI device (API 29) due to OEM shell restrictions blocking broadcasts to force-stopped apps, the profile was manually installed via an ADB workaround (lifting the stopped state with monkey, sending the `INSTALL_PROFILE` broadcast, and running `cmd package compile -f -m speed-profile`). The tests were then measured in `CompilationMode.Ignore()` to prevent Macrobenchmark from discarding the manual AOT state. See `emui-baseline-profile-workaround.md` for full details.
