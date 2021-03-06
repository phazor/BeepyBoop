# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

-keep public class com.phazor.beepy.MainActivity

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.okhttp3.**

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keep interface retrofit2.** { *;}
-dontwarn rx.**
-dontwarn retrofit2.**

## Disable proguard
# dontoptimize 
# -dontshrink 
# -dontusemixedcaseclassnames 
# -dontskipnonpubliclibraryclasses 
# -dontpreverify 
# -verbose

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
