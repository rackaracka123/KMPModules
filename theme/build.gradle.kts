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

val githubProperties = readProperties(file("github.properties"))
val username: String? by githubProperties
val password: String? by githubProperties

publishing {
    group = "se.alster.kmp"
    version = "1.0.0" // TODO: Make this automatic
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/alsterverse/ ÄNDRA DENNA SENARE")
            credentials {
                username = username ?: System.getenv("GITHUB_ACTOR")
                password = password ?: System.getenv("GITHUB_TOKEN")
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
