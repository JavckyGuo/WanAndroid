plugins {
    alias(libs.plugins.android.library)
    // kotlin-android intentionally not applied; AGP 9.0+ has built-in Kotlin support.
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
    // MainDispatcherRule/FakeDispatcherProvider need to be visible to OTHER modules'
    // test source sets (e.g. :feature:home's tests). A module's plain src/test/ is
    // private to that module's own test compilation -- other modules depending on
    // :core only see its main source set, never its test source set, so
    // `testImplementation(project(":core"))` alone leaves "Unresolved reference"
    // errors on these classes. AGP's testFixtures source set is the supported way
    // to share test-only code across module boundaries.
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
