package com.crazy_coder.everfit_wear.data.model

import com.google.gson.annotations.SerializedName

data class WorkoutResponse(
    @SerializedName("data")
    val data: List<Workout>,
)