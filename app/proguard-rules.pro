# ProGuard rules for Pulse — Ultimate Sensor Toolbox
# Keep Google Mobile Ads and UMP
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.android.ump.** { public *; }

# Keep data models
-keepclassmembers class com.cartoniq.pulse.sensor.** { *; }
-keepclassmembers class com.cartoniq.pulse.domain.** { *; }
