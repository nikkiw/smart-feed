# EMUI Baseline Profile Workaround

When running Android Macrobenchmark with `CompilationMode.Partial()` on non-rooted Huawei devices (EMUI / Android 10), the benchmark will fail with the following exception:

```text
The baseline profile install broadcast was not received. This most likely means that the profileinstaller library is missing from the target apk.
```

## The Cause

Macrobenchmark attempts to compile the app using the Baseline Profile before running the benchmark. To do this, it:
1. Force-stops the application package.
2. Sends an `INSTALL_PROFILE` explicit broadcast to `androidx.profileinstaller.ProfileInstallReceiver` inside the app.
3. Compiles the package using `cmd package compile -m speed-profile`.

However, on EMUI (and potentially other aggressive OEM battery management forks), **broadcasts directed to force-stopped applications are blocked at the OS level**. Because the application never receives the broadcast, the profile is not written to disk, and the `Partial()` compilation mode fails. 

Setting `CompilationMode.Ignore()` out of the box avoids the crash but results in a **completely uncompiled (Cold JIT) execution**, heavily skewing benchmark results (specifically inflating P90 and P99 rendering times).

## The Workaround (No-Root ADB)

To accurately measure the baseline profile without root, we must manually prepare the profile compilation state via ADB, bypassing the EMUI broadcast restrictions, and then configure Macrobenchmark to `Ignore()` the state so it doesn't drop our manual compilation.

### 1. Install the Benchmark Target APK
Ensure your target APK (e.g. the `benchmark` or `release` variant) is freshly installed on the device.
```bash
./gradlew :app:installDevBenchmark
```

### 2. Remove the "Stopped" State
To allow EMUI to deliver broadcasts to the app, it must be launched at least once. We can trigger a launch via the `monkey` command and immediately send it to the background.
```bash
PKG="com.ndev.android.smart.feed"
RECEIVER="androidx.profileinstaller.ProfileInstallReceiver"

# Start the app briefly
adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1
sleep 2

# Send it to the background
adb shell input keyevent HOME
```

### 3. Send the Install Broadcast
Send the ProfileInstaller broadcast, explicitly including stopped packages just in case.
```bash
adb shell am broadcast \
  --include-stopped-packages \
  -a androidx.profileinstaller.action.INSTALL_PROFILE \
  -n "$PKG/$RECEIVER"
```
You should see:
```text
Broadcast completed: result=1
```
*(A result of `1` means `RESULT_INSTALL_SUCCESS`)*

### 4. Force-Stop and Compile
Now that the profile is written, force-stop the app so the `speed-profile` compilation can execute safely.
```bash
# Stop the process
adb shell am force-stop "$PKG"

# Compile only the methods from the written profile
adb shell cmd package compile -f -m speed-profile "$PKG"
```
You should see:
```text
Success
```

### 5. Run the Benchmark in Ignore Mode
Update your Macrobenchmark test to use `CompilationMode.Ignore()`. This tells the benchmark framework to leave the compilation state exactly as we manually set it, rather than trying to clear it.

```kotlin
@Test
fun feedListScrollWithProfile() {
    benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Ignore(), // Crucial: Preserves manual AOT
        startupMode = StartupMode.COLD,
        iterations = 10,
        ...
    )
}
```

Now, run the benchmark. The resulting trace will correctly reflect the AOT speed-profile execution on the Huawei device.
