plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    // NOTE: org.jetbrains.kotlin.android ("kotlin-android") is intentionally NOT applied.
    // AGP 9.0+ has built-in Kotlin support for android.application/android.library modules;
    // applying kotlin-android on top of it now fails the build with:
    // "The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support
    //  since AGP 9.0." See https://kotl.in/gradle/agp-built-in-kotlin
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
