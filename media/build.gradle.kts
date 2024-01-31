import java.net.URI
import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    id("maven-publish")
}

fun readProperties(propertiesFile: File) = Properties().apply {
    if (!propertiesFile.exists()) propertiesFile.createNewFile()
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}

val githubProperties = readProperties(rootProject.file("github.properties"))

publishing {
    group = "se.alster.kmp"
    version = githubProperties.getProperty("version") ?: System.getenv("VERSION")
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/alsterverse/AlsterKMPModules")
            credentials {
                username = githubProperties.getProperty("username") ?: System.getenv("GITHUB_ACTOR")
                password = githubProperties.getProperty("token")  ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kotlin {
    androidTarget()
    jvmToolchain(17)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "media"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.qr.core)
            implementation(libs.androidx.camera.camera2)
        }
    }
}

android {
    namespace = "se.alster.kmp.media"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    buildTypes {
        debug {

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
