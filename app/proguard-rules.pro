# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn java.lang.invoke.StringConcatFactory

# Keep ProfileInstallReceiver so macrobenchmark's DROP_SHADER_CACHE broadcast
# reaches it even when R8 minification is enabled (benchmark build type).
-keep class androidx.profileinstaller.ProfileInstallReceiver { *; }