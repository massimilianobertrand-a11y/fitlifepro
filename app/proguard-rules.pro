
# Apache POI
-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**

# poi-ooxml transitive dependencies not available on Android
# Saxon XSLT/XQuery engine (optional in xmlbeans, not needed on Android)
-dontwarn net.sf.saxon.**
# StAX API (javax.xml.stream) not present in Android SDK
-dontwarn javax.xml.stream.**
# AWT desktop classes referenced by poi/graphbuilder
-dontwarn java.awt.**
# OSGi framework (referenced by log4j, not used on Android)
-dontwarn org.osgi.**
-dontwarn aQute.bnd.**
# graphbuilder curve library
-dontwarn com.graphbuilder.**
# xmlbeans
-dontwarn org.apache.xmlbeans.**
-keep class org.apache.xmlbeans.** { *; }
# log4j OSGi service locator
-dontwarn org.apache.logging.log4j.**

# Health Connect
-keep class androidx.health.connect.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel { *; }
