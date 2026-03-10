plugins {
    // Explicitly declaring plugins is not strictly required if defined in settings.gradle.kts
    // with version management, but it's good practice for clarity.
    // Versions are defined in settings.gradle.kts
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    kotlin("multiplatform") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
