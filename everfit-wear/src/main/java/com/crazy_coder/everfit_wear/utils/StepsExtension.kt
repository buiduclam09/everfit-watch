package com.crazy_coder.everfit_wear.utils

import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.IntervalDataPoint

fun List<IntervalDataPoint<Long>>.latestSteps(): Long? {
    return this
        // dataPoints can have multiple types (e.g. if the app is registered for multiple types).
        .filter { it.dataType == DataType.STEPS }
        // where accuracy information is available, only show readings that are of medium or
        // high accuracy. (Where accuracy information isn't available, show the reading if it is
        // a positive value).
        .filter { it.value > 0 }
        .maxByOrNull { it.endDurationFromBoot }?.value
}