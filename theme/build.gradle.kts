import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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
            url = URI("https://maven.pkg.github.com/rackaracka123/KMPModules")
            credentials {
                username = githubProperties.getProperty("username") ?: System.getenv("GITHUB_ACTOR")
                password = githubProperties.getProperty("token")  ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    jvmToolchain(17)
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "theme"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
        }
    }
}

android {
    namespace = "se.alster.kmp.theme"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}
