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

-keep class com.riady.printerlib.Activity.DeviceListActivity
-keepclassmembers class com.riady.printerlib.Activity.DeviceListActivity {
    public *;
}
-keep class com.riady.printerlib.Printer
-keepclassmembers class com.riady.printerlib.Printer {
    public *;
}
-keep class com.riady.printerlib.Service.BluetoothService
-keepclassmembers class com.riady.printerlib.Service.BluetoothService {
    public *;
}
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile