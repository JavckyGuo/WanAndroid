// Domain 模块只承载领域模型、仓库契约和用例，不依赖具体数据实现。
plugins {
    alias(libs.plugins.android.library)
    // AGP 9 已内置 Kotlin，不再重复应用 kotlin-android 插件。
}

android {
    namespace = "com.jacky.wanandroid.domain"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
