package com.lambui.everfit.data.model

import com.google.gson.annotations.SerializedName

data class Assignment(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("status")
    val status: Int?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("exercises_count")
    val exercisesCount: Int?,
)