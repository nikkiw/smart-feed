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
}

gradlePlugin {
    plugins {
        register("androidLibraryWithHilt") {
            // ID, по которому вы будете подключать плагин в модулях
            id = "ndev-android-library-with-hilt-convention"

            // полное имя класса с пакетом, где лежит ваш Plugin<Project>
            implementationClass = "com.ndev.convention.AndroidLibraryWithHiltConventionPlugin"
        }

        register("androidApplicationWithHilt") {
            // ID, по которому вы будете подключать плагин в модулях
            id = "ndev-android-application-with-hilt-convention"

            // полное имя класса с пакетом, где лежит ваш Plugin<Project>
            implementationClass = "com.ndev.convention.AndroidApplicationWithHiltConventionPlugin"
        }
    }
}