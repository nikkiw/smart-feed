pluginManagement {
    includeBuild("build-logic")
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SmartFeedMVP"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":mock-server")
include(":core:core")
include(":core:core-database")
include(":core:core-networks")
include(":core:core-data")
include(":core:core-domain")
include(":core:image-glide")
include(":feature:feed")
