# ===== ChronoMark 混淆规则 =====

# 保留 Room 数据库相关类
-keep class io.github.chy5301.chronomark.data.database.** { *; }
-keepclassmembers class io.github.chy5301.chronomark.data.database.** { *; }

# 保留 DataStore Preferences
-keep class androidx.datastore.preferences.** { *; }
-keepclassmembers class androidx.datastore.preferences.** { *; }

# 保留 Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.chy5301.chronomark.**$$serializer { *; }
-keepclassmembers class io.github.chy5301.chronomark.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.chy5301.chronomark.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留数据模型类（用于序列化）
-keep class io.github.chy5301.chronomark.data.model.** { *; }
-keepclassmembers class io.github.chy5301.chronomark.data.model.** { *; }

# 保留 Compose 相关
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# 保留 ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# 保留 Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留调试信息（可选，生产环境可注释掉以进一步减小体积）
-keepattributes SourceFile,LineNumberTable

# 移除日志（可选，生产环境建议启用以提升性能）
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }

# ===== OkHttp 混淆规则 =====
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 保留网络相关模型
-keep class io.github.chy5301.chronomark.data.network.** { *; }
-keepclassmembers class io.github.chy5301.chronomark.data.network.** { *; }
