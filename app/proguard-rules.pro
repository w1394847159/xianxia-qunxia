# 修仙群侠传 ProGuard Rules
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class com.xianxia.qunxia.game.** { *; }
-keep class com.xianxia.qunxia.data.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
