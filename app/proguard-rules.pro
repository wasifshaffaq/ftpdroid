# FTPDroid ProGuard Rules

# Apache FTP Server
-keep class org.apache.ftpserver.** { *; }
-keep class org.apache.mina.** { *; }
-dontwarn org.apache.ftpserver.**
-dontwarn org.apache.mina.**

# Apache Commons Net
-keep class org.apache.commons.net.** { *; }
-dontwarn org.apache.commons.net.**

# sshj (SFTP)
-keep class net.schmizz.sshj.** { *; }
-keep class com.hierynomus.sshj.** { *; }
-dontwarn net.schmizz.**
-dontwarn com.hierynomus.**

# Bouncycastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# SLF4J
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keepclasseswithmembernames class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

# ZXing QR
-keep class com.google.zxing.** { *; }

# Keep domain models
-keep class com.ftpdroid.app.domain.model.** { *; }
