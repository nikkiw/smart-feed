package com.core.data.work

import java.time.Duration


interface WorkerScheduleConfig {
    val fetchInterval: Duration
    val fetchFlex: Duration
}