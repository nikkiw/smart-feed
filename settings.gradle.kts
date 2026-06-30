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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
include(":core:core-paging")
include(":core:core-data")
include(":core:core-domain")
include(":core:coroutines")
include(":core:lifecycle")
include(":core:connectivity")
include(":core:image:api")
include(":core:image-glide")
include(":feature:feed:api")
include(":feature:feed:impl")
include(":architecture-tests")
