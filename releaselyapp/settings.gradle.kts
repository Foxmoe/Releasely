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
        kotlin("multiplatform") version "1.9.22" apply false
        kotlin("android") version "1.9.22" apply false
        id("com.android.application") version "8.2.0" apply false
        id("com.android.library") version "8.2.0" apply false
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