pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")

        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "2.0.20" apply false
        kotlin("android") version "2.0.20" apply false
        kotlin("plugin.serialization") version "2.0.20" apply false
        kotlin("plugin.compose") version "2.0.20" apply false
        id("org.jetbrains.compose") version "1.7.3" apply false
        id("com.android.application") version "8.12.0" apply false
        id("com.android.library") version "8.12.0" apply false
        id("app.cash.sqldelight") version "2.0.2" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")

        google()
        mavenCentral()
    }
}

rootProject.name = "releaselyapp"
include(":app")
include(":shared")