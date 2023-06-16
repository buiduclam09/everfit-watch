package com.crazy_coder.everfit_wear.utils

import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.data.SampleDataPoint

fun List<SampleDataPoint<Double>>.latestHeartRate(): Double? {
    return this
        // dataPoints can have multiple types (e.g. if the app is registered for multiple types).
        .filter { it.dataType == DataType.HEART_RATE_BPM }
        // where accuracy information is available, only show readings that are of medium or
        // high accuracy. (Where accuracy information isn't available, show the reading if it is
        // a positive value).
        .filter {
            it.accuracy == null ||
                    setOf(
                        HeartRateAccuracy.SensorStatus.ACCURACY_HIGH,
                        HeartRateAccuracy.SensorStatus.ACCURACY_MEDIUM
                    ).contains((it.accuracy as HeartRateAccuracy).sensorStatus)
        }
        .filter { it.value > 0 }
        .maxByOrNull { it.timeDurationFromBoot }?.value
}