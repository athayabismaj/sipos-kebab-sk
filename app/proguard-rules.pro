# ============================================================
# Kebab SK — ProGuard / R8 Rules
# ============================================================

# --- Debugging (uncomment for crash traces in release) ---
# -keepattributes SourceFile,LineNumberTable
# -renamesourcefileattribute SourceFile

# --- Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# --- Retrofit & OkHttp ---
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# --- Gson: Protect all data model classes from obfuscation ---
-keep class com.sipos.kebabsk.feature.**.data.remote.** { *; }
-keep class com.sipos.kebabsk.feature.**.domain.model.** { *; }
-keep class com.sipos.kebabsk.feature.**.presentation.*UiState { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Compose: Prevent stripping of stable/immutable markers ---
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# --- Splash Screen ---
-keep class androidx.core.splashscreen.** { *; }

# --- App specific: Keep Application & Activities ---
-keep class com.sipos.kebabsk.SiposKebabApplication { *; }
-keep class com.sipos.kebabsk.MainActivity { *; }