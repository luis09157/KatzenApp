# Add project-specific ProGuard rules here.
# https://developer.android.com/studio/build/shrink-code

# ==========================
# General Debugging Rules
# ==========================

-keepattributes SourceFile,LineNumberTable

# ==========================
# Gson Rules
# ==========================

-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.example.katzen.Model.** { *; }

# ==========================
# Firebase Rules
# ==========================

-keepclassmembers class com.example.katzen.Model.** {
    public <init>();
}

-keep class com.google.firebase.** { *; }

# ==========================
# Parcelable (@Parcelize) Rules
# ==========================

-keep class kotlinx.parcelize.** { *; }
-keep class android.os.Parcelable { *; }
-keepclassmembers class com.example.katzen.Model.** {
    public <init>();
}

# ==========================
# Coil
# ==========================

-dontwarn coil.**
-keep class coil.** { *; }

# ==========================
# Other Compatibility Rules
# ==========================

-keepattributes InnerClasses
-keep class *$* { *; }
