# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontobfuscate
-keep,allowoptimization class is.xyz.mpv.** { public protected *; }

# navigation3-ui alpha06 calls the Compose overload NavigationEventHandlerKt
# .NavigationEventHandler(Function0, Function2, Composer, int, int).
# R8's aggressive optimization (proguard-android-optimize.txt) moved/mangled that
# method — 0.1.26 shipped an EMPTY NavigationEventHandlerKt and 0.1.30 had the
# method in androidx.tracing.Trace with a rewritten signature — causing the
# launch NoSuchMethodError / blank first frame. Keep the original ABI.
-keep class androidx.navigationevent.compose.NavigationEventHandlerKt { *; }
-keep class androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner { *; }
-keep class androidx.navigation3.ui.** { *; }
-keep class androidx.navigation3.runtime.** { *; }