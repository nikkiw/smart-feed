# XML performance baseline

This baseline must be captured before the first Compose island is merged. The same
benchmarks and device protocol must then be reused for the islands and full Compose
variants.

## Scope

Two release-optimized variants are used for different purposes:

- `devBenchmark`: deterministic data and the primary XML/Islands/Compose runtime comparison.
- `prodBenchmark`: production flavor cross-check for startup and release behavior.
- `prodRelease`: distributable build used for final APK-size and R8/resource-shrinking numbers.
- UI: current View Binding + XML + `RecyclerView` implementation.
- Data source: deterministic `core/core-networks/src/dev/assets/articles.json`.
- Compilation mode: `CompilationMode.None()`.
- Startup: cold process startup, 10 iterations.
- Scroll: five downward and five upward flings, 10 iterations.
- The scroll benchmark exists only in the `dev` benchmark source set and pre-warms
  the visible feed and image cache before measuring.
- `benchmark` is copied from `release`, is minified/resource-shrunk and is signed with
  the debug key only for local installation. JaCoCo coverage is disabled for every
  non-debug build type.

`CompilationMode.None()` intentionally records the uncompiled baseline. Keep this
mode unchanged for the XML, islands and full Compose comparisons. Add a separate
Baseline Profile comparison later instead of replacing this baseline.

## Run

Use a physical device with animations disabled and no thermal throttling. Do not
compare results collected from different device models.

```bash
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

./gradlew :app:assembleDevBenchmark
./gradlew :benchmark:connectedDevBenchmarkAndroidTest

# Production-flavor startup cross-check. FeedScrollBenchmark is intentionally absent.
./gradlew :app:assembleProdBenchmark
./gradlew :benchmark:connectedProdBenchmarkAndroidTest

# Actual distributable release artifact for size/R8 verification.
./gradlew :app:assembleProdRelease
```


## Verify release-equivalent configuration

The target package must not contain `DEBUGGABLE`, and the benchmark APK must be
minified like `prodRelease`. Do not suppress Macrobenchmark's `DEBUGGABLE` error.

```bash
./gradlew :app:assembleDevBenchmark :app:assembleProdBenchmark :app:assembleProdRelease

DEV_APK=app/build/outputs/apk/dev/benchmark/app-dev-benchmark.apk
PROD_BENCHMARK_APK=app/build/outputs/apk/prod/benchmark/app-prod-benchmark.apk
PROD_RELEASE_APK=app/build/outputs/apk/prod/release/app-prod-release.apk

for apk in "$DEV_APK" "$PROD_BENCHMARK_APK" "$PROD_RELEASE_APK"; do
  "$ANDROID_HOME/cmdline-tools/latest/bin/apkanalyzer" manifest print "$apk" \
    | grep -E 'package=|debuggable|profileable'
done
```

Expected:

- `devBenchmark` and `prodBenchmark`: `debuggable=false`, `profileable shell=true`;
- `prodRelease`: `debuggable=false` and no benchmark-only `profileable` overlay;
- R8 mapping files exist for all three optimized variants under
  `app/build/outputs/mapping/<variant>/mapping.txt`.

Use `devBenchmark` for the controlled UI comparison. Use `prodBenchmark` only as a
production-flavor runtime cross-check. Report distributable size from `prodRelease`,
not from the benchmark APK, because the latter is debug-signed and contains the
profileable benchmark manifest overlay.

## Record the environment

Save this metadata next to the JSON results:

```bash
git rev-parse HEAD
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell dumpsys display | grep -i -E 'refresh|fps'
adb shell dumpsys battery | grep -E 'level|temperature|status'
```

Record at minimum:

| Field | Value |
|---|---|
| Git commit | |
| Device and Android build | |
| Refresh rate | |
| Battery level / temperature | |
| Benchmark library | 1.4.1 |
| Primary comparison variant | devBenchmark |
| Production cross-check | prodBenchmark / prodRelease |
| Iterations | 10 |

## Metrics to copy into the article dataset

### Startup

- `timeToInitialDisplayMs`: min, median and max.
- `timeToFullDisplayMs`: only after the app reports fully drawn; until then do not
  present TTID as TTFD.

### Feed scroll

- `frameDurationCpuMs`: P50, P90, P95 and P99.
- `frameOverrunMs`: P50, P90, P95 and P99.
- Number of frames with positive `frameOverrunMs`.


## APK size and build-time baseline

```bash
/usr/bin/time -p ./gradlew --no-build-cache clean \
  :app:assembleDevBenchmark :app:assembleProdRelease
/usr/bin/time -p ./gradlew --no-build-cache \
  :app:assembleDevBenchmark :app:assembleProdRelease

DEV_BENCHMARK_APK=app/build/outputs/apk/dev/benchmark/app-dev-benchmark.apk
PROD_RELEASE_APK=app/build/outputs/apk/prod/release/app-prod-release.apk
wc -c "$DEV_BENCHMARK_APK" "$PROD_RELEASE_APK"
```

Use the same Gradle daemon and build-cache policy for every migration stage. The
first command records a clean build; the second records a no-change build and is not
an incremental source-edit benchmark.

## Freeze the baseline

After results are archived and reviewed:

```bash
git tag -a compose-study-xml-baseline -m "XML UI performance baseline"
```

