package com.crazy_coder.everfit_wear.data.model

import java.util.Date

data class WeekdayItem(
    val date: Date,
    val workoutId: String?,
    val assignments: List<AssignmentItem>,
)