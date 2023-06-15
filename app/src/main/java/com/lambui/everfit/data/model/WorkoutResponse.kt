package com.lambui.everfit.data.model

import com.google.gson.annotations.SerializedName

data class WorkoutResponse(
    @SerializedName("data")
    val data: List<Workout>,
)