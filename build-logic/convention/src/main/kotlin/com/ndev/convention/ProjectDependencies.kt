package com.ndev.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureDependenciesHilt() {
    val libs = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    dependencies {
        add("implementation", libs.findLibrary("hilt.android.core").get())
        add("ksp", libs.findLibrary("hilt.compiler").get())
    }
}

internal fun Project.configureDependenciesUnitTests() {
    val libs = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    dependencies {
        add("testImplementation", libs.findLibrary("junit4").get())
        add("testImplementation", libs.findLibrary("kotlin.test").get())
        add("testImplementation", libs.findLibrary("kotlin.coroutines.test").get())
        add("testImplementation", libs.findLibrary("mockk").get())

        // hilt testing
        add("testImplementation", libs.findLibrary("hilt-android-testing").get())
        add("kspTest", libs.findLibrary("hilt.compiler").get())

    }
}

internal fun Project.configureDependenciesAndroidTests() {
    val libs = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    dependencies {
        add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
        add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
        add("androidTestImplementation", libs.findLibrary("kotlin.test").get())
        add("androidTestImplementation", libs.findLibrary("kotlin.coroutines.test").get())

        // hilt testing
        add("androidTestImplementation", libs.findLibrary("hilt-android-testing").get())
        add("kspAndroidTest", libs.findLibrary("hilt.compiler").get())
    }
}

internal fun Project.configureDependenciesCoreLibraryDesugaring() {
    val libs = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("desugar.jdk.libs").get())
    }
}



internal fun Project.configureDependenciesFeature() {
    val libs = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    dependencies {
        add("implementation", libs.findLibrary("kotlinx.coroutines.core").get())

        add("implementation", libs.findBundle("decompose.libs").get())
        add("implementation", libs.findLibrary("material").get())
        add("implementation", libs.findLibrary("swiperefreshlayout").get())

    }
}


internal fun Project.configureDependenciesKotlinxSerialization() {
    val libs = extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

    dependencies {
        add("implementation", libs.findLibrary("kotlinx.serialization.json").get())
    }
}




