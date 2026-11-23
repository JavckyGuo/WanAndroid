// 根工程只声明插件版本；各模块按职责选择应用，避免无关插件进入构建图。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    // AGP 9 已为 Android 模块内置 Kotlin 支持，重复应用 kotlin-android 会导致构建失败。
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
