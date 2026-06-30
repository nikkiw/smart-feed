plugins {
    `kotlin-dsl` // use gradlePlugin to register the plugin we created, which helps gradle to discover our plugins
}

group = "com.ndev.smart.feed.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationWithHilt") {
            id = libs.plugins.smart.feed.android.application.asProvider().get().pluginId
            implementationClass = "com.ndev.convention.AndroidApplicationWithHiltConventionPlugin"
        }

        register("androidApplicationJacoco") {
            id = libs.plugins.smart.feed.android.application.jacoco.get().pluginId
            implementationClass = "com.ndev.convention.AndroidApplicationJacocoConventionPlugin"
        }


        register("androidLibraryWithHilt") {
            id = libs.plugins.smart.feed.android.library.asProvider().get().pluginId
            implementationClass = "com.ndev.convention.AndroidLibraryWithHiltConventionPlugin"
        }

        register("androidLibraryJacoco") {
            id = libs.plugins.smart.feed.android.library.jacoco.get().pluginId
            implementationClass = "com.ndev.convention.AndroidLibraryJacocoConventionPlugin"
        }


        register("androidFeature") {
            id = libs.plugins.smart.feed.android.feature.get().pluginId
            implementationClass = "com.ndev.convention.AndroidFeatureConventionPlugin"
        }

        register("detektConvention") {
            id = "smart.feed.detekt"
            implementationClass = "com.ndev.convention.DetektConventionPlugin"
        }

        register("spotlessConvention") {
            id = "smart.feed.spotless"
            implementationClass = "com.ndev.convention.SpotlessConventionPlugin"
        }

    }
}
