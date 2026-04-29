import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("app.cash.sqldelight")
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("top.foxmoe.releasely.database")
        }
    }
}

// 基于 Git 提交数自动生成 versionCode，确保每次构建版本号递增
val gitCommitCount = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    workingDir = rootDir
}.standardOutput.asText.get().trim().toInt()

val versionMajor = 1
val versionMinor = 0
val versionPatch = gitCommitCount

// 从 local.properties 或环境变量读取签名配置（优先 local.properties，便于 CI 注入）
val localProps = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        load(localFile.inputStream())
    }
}

fun signingProp(key: String, envVar: String): String? {
    return localProps.getProperty(key)?.ifBlank { null }
        ?: System.getenv(envVar)?.ifBlank { null }
}

android {
    namespace = "top.foxmoe.releasely"
    compileSdk = 34

    defaultConfig {
        applicationId = "top.foxmoe.releasely"
        minSdk = 24
        targetSdk = 34
        versionCode = gitCommitCount
        versionName = "$versionMajor.$versionMinor.$versionPatch"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = signingProp("RELEASE_STORE_FILE", "RELEASE_STORE_FILE")
            val storePass = signingProp("RELEASE_STORE_PASSWORD", "RELEASE_STORE_PASSWORD")
            val keyAlias = signingProp("RELEASE_KEY_ALIAS", "RELEASE_KEY_ALIAS")
            val keyPass = signingProp("RELEASE_KEY_PASSWORD", "RELEASE_KEY_PASSWORD")

            if (storeFilePath != null && storePass != null && keyAlias != null && keyPass != null) {
                storeFile = file(storeFilePath)
                this.storePassword = storePass
                this.keyAlias = keyAlias
                this.keyPassword = keyPass
            } else {
                println("[WARN] Release signing config incomplete — building unsigned release APK. " +
                    "Set RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD " +
                    "in local.properties or environment variables.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 仅在签名配置完整时使用 release 签名，否则回退到 debug 签名（本地测试）
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // 如果使用 Compose，需要开启
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // --- 关键：依赖 shared 模块 ---
    implementation(project(":shared"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose 依赖 (可选，如果你用 XML 可以去掉)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("app.cash.sqldelight:android-driver:2.0.1")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}