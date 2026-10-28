
try {
    val processEnvironment = Class.forName("java.lang.ProcessEnvironment")
    val theEnvironmentField = processEnvironment.getDeclaredField("theEnvironment").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    (theEnvironmentField.get(null) as? MutableMap<String, String>)?.remove("ANDROID_PREFS_ROOT")
    val theCaseInsensitiveEnvironmentField = processEnvironment.getDeclaredField("theCaseInsensitiveEnvironment").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    (theCaseInsensitiveEnvironmentField.get(null) as? MutableMap<String, String>)?.remove("ANDROID_PREFS_ROOT")
} catch (_: Throwable) {}

pluginManagement {
    repositories {
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CupBank"
include(":app")
