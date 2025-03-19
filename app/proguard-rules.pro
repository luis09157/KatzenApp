# Add project-specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see:
#   https://developer.android.com/studio/build/shrink-code

# ==========================
# 📌 General Debugging Rules
# ==========================

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide original source file name in stack traces (optional)
#-renamesourcefileattribute SourceFile

# ==========================
# 📌 Gson Rules
# ==========================

# Keep generic type information for Gson
-keepattributes Signature

# Prevent stripping TypeToken class, needed for Gson
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep all data models that use Gson (adjust as necessary)
-keep class com.ninodev.rutasmagicas.Model.** { *; }
-keep class com.example.katzen.Model.PacienteCampañaModel { *; }

# ==========================
# 📌 Firebase Rules
# ==========================

# Prevent Firebase from stripping no-argument constructors
-keepclassmembers class com.ninodev.katzen.Model.** {
    public <init>();
}

# Keep all Firebase data models
-keep class com.ninodev.katzen.Model.** { *; }

# Keep Firebase SDK classes
-keep class com.google.firebase.** { *; }

# ==========================
# 📌 Parcelable (@Parcelize) Rules
# ==========================

# Keep Parcelable implementations to avoid issues with @Parcelize
-keep class kotlinx.parcelize.** { *; }
-keep class android.os.Parcelable { *; }
-keepclassmembers class com.example.katzen.Model.* {
    public <init>();
}

# ==========================
# 📌 Other Compatibility Rules
# ==========================

# Keep inner and anonymous classes to prevent deserialization issues
-keepattributes InnerClasses
-keep class *$* { *; }

# Prevent removal of lambda functions needed for Firebase and Gson
-dontwarn java.lang.invoke.LambdaMetafactory

