# Kotlin Multiplatform Module

This Kotlin Multiplatform module facilitates seamless integration with your projects, providing a common codebase across multiple platforms. To ensure secure access to releases, follow the steps below:

## Authorization Setup

1. Create a file in the root project directory named `github.properties`.

2. Add your GitHub username and token to the `github.properties` file in the following format:
    ```
    username=your_github_username
    token=your_github_token
    ```
Make sure `your_github_token` has permissions to read packages. For more information, see [Creating a personal access token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).
## Configure Settings

3. In the `settings.gradle` file, add the following code to read the GitHub credentials:

    ```kotlin
    val githubProperties = readProperties(file("github.properties"))
    val username: String? by githubProperties
    val token: String? by githubProperties
    ```

4. Add the GitHub Packages Maven repository to your `settings.gradle`:

    ```kotlin
    maven {
        name = "GitHubPackages"
        url = java.net.URI("https://maven.pkg.github.com/alsterverse/AlsterKMPModules")
        credentials {
            username = username
            password = token
        }
    }
    ```

## Dependency Integration

5. Import the module into your project's `build.gradle.kts` (or equivalent) file:

    ```kotlin
    commonMain.dependencies {
        implementation("se.alster.kmp:theme:<version>")
    }
    ```
Now, your Kotlin Multiplatform module is configured to fetch dependencies from GitHub Packages. This ensures smooth integration and access to the latest releases. Make sure to keep your GitHub credentials secure and not share them in public repositories.
