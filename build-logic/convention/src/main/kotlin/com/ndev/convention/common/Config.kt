package com.ndev.convention.common

import org.gradle.api.JavaVersion

object Config {
    const val APPLICATION_ID = "com.ndev.android.smart.feed"
    const val NAMESPACE = "com.ndev.android.smart.feed"
    const val COMPILE_SDK = 35
    const val MIN_SDK = 23
    const val TARGET_SDK = 35

    val COMPILE_JAVA_VERSION = JavaVersion.VERSION_17
    const val JVM_TARGET = "17"
}