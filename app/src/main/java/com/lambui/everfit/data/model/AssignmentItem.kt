package com.lambui.everfit.data.model

data class AssignmentItem(
    val id: String,
    val workoutId: String,
    val title: String,
    val exerciseCount: Int,
    val status: AssignmentStatus,
)

enum class AssignmentStatus {
    Assigned,
    Missed,
    Completed,
    Upcoming,
}