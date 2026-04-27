plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("io.insert-koin:koin-core:3.5.6")

                implementation("io.ktor:ktor-client-core:2.3.11")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
                implementation("io.ktor:ktor-client-logging:2.3.11")

                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.11")
                implementation("app.cash.sqldelight:android-driver:2.0.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            }
        }
    }
}

android {
    namespace = "top.foxmoe.releasely.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("ReleaselyDatabase") {
            packageName.set("top.foxmoe.releasely.shared.db")
        }
    }
}