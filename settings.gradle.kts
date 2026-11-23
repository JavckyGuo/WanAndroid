// 统一插件与依赖仓库，并在此登记应用、分层和业务模块。
pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenCentral()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        google()
        mavenCentral()
    }
}

rootProject.name = "WanAndroid"
include(
    ":app", ":core", ":core:designsystem", ":domain", ":data",
    ":feature:home", ":feature:knowledge", ":feature:navigation",
    ":feature:project", ":feature:search", ":feature:auth",
    ":feature:collection", ":feature:profile", ":feature:web"
)
