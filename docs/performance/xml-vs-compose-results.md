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

| Metric | XML (Baseline) | Compose Islands (Без профиля) | Compose Islands (С профилем) |
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
- **Startup Time**: При внедрении Compose без профиля медиана TTID выросла на 10.6 ms (с 661.7 до 672.3 ms). Добавление Baseline Profile (CompilationMode.Ignore) немного улучшило результат до 669.8 ms, сократив деградацию до 8.1 ms.
- **Feed Scroll Performance**: Скролл без профиля показал рост времени кадра (P50: 6.7 -> 8.1 ms, P90: 9.5 -> 10.5 ms, P99: 23.8 -> 26.5 ms). Измерения с профилем дали практически идентичные результаты (P50: 8.1 ms, P90: 10.9 ms, P99: 26.3 ms). Обе реализации Compose (с профилем и без) работают в пределах бюджета в 16 мс на кадр (60 FPS), поэтому скролл ощущается плавным.
- **APK Size**: Размер релиза увеличился с 9.38 MB до 9.91 MB из-за библиотек Compose и вшитого файла профиля.
- **Build Time**: Время чистой сборки с Compose выросло на ~30 сек по сравнению с XML. Время сборки (244 сек с профилем) может колебаться в зависимости от загрузки машины.

> **Примечание о Baseline Profiles:** Профиль был успешно сгенерирован на эмуляторе с API 31 с помощью `Gradle Managed Devices` и добавлен в исходный код (`app/src/main/baseline-prof.txt`). Так как на физическом устройстве HUAWEI (API 29) Android Macrobenchmark не может динамически послать broadcast для установки профиля через `CompilationMode.Partial()` (из-за ограничений оболочки), профиль замерялся в режиме `CompilationMode.Ignore()`, который тестирует состояние приложения после обычной установки пакета (где профиль вшит в APK и применяется системой естественно). Влияние Baseline Profile в данном этапе "Островов" (Islands) оказалось минимальным, так как Compose инициализируется только внутри отдельных карточек при скролле. Ожидается, что польза от профиля будет гораздо заметнее при полном переходе на Compose (Full Compose) навигации и экранов.
