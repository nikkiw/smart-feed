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
include(":core:content:api")
include(":core:coroutines")
include(":core:lifecycle")
include(":core:connectivity")
include(":core:analytics:api")
include(":core:analytics:local")
include(":core:analytics:impl")
include(":core:image:api")
include(":core:image-glide")
include(":feature:feed:api")
include(":feature:feed:local")
include(":feature:feed:impl")
include(":feature:recommendation:api")
include(":feature:recommendation:local")
include(":feature:recommendation:impl")
include(":feature:userprofile:api")
include(":feature:userprofile:local")
include(":feature:userprofile:impl")
include(":architecture-tests")
