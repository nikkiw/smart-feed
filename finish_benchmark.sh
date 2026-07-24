#!/bin/bash
set -e

# Ensure animations are restored even if the script fails
trap "echo 'Restoring animations...'; adb shell settings put global window_animation_scale 1; adb shell settings put global transition_animation_scale 1; adb shell settings put global animator_duration_scale 1" EXIT

echo "Running prodBenchmark..."
./gradlew :app:assembleProdBenchmark
./gradlew :benchmark:connectedProdBenchmarkAndroidTest

echo "Running clean build time test..."
/usr/bin/time -p ./gradlew --no-build-cache clean :app:assembleDevBenchmark :app:assembleProdRelease

echo "Checking APK sizes..."
DEV_BENCHMARK_APK=app/build/outputs/apk/dev/benchmark/app-dev-benchmark.apk
PROD_RELEASE_APK=app/build/outputs/apk/prod/release/app-prod-release.apk
wc -c "$DEV_BENCHMARK_APK" "$PROD_RELEASE_APK"
ls -lh "$DEV_BENCHMARK_APK" "$PROD_RELEASE_APK"
