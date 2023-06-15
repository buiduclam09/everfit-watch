package com.lambui.everfit.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Workout(
    @SerializedName("_id")
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,

    @ColumnInfo("assignments")
    @SerializedName("assignments")
    val assignments: List<Assignment>,
)