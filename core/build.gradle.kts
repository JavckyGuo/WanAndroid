// Core 模块提供调度器、基础 ViewModel、日志及跨模块测试夹具。
plugins {
    alias(libs.plugins.android.library)
    // AGP 9 已内置 Kotlin，不再重复应用 kotlin-android 插件。
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jacky.wanandroid.core"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Test Fixtures 将 MainDispatcherRule 等测试基础设施安全共享给 Feature 测试。
    testFixtures { enable = true }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.kotlinx.coroutines.test)
}
