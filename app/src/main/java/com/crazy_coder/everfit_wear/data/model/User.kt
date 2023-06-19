package com.crazy_coder.everfit_wear.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class User(
    val id: Long,
    val userName: String,
    val supervisor: Boolean,
    val objectID: @RawValue Any? = null,
    val realName: String,
    val facility: Long,
    val building: Long
) : Parcelable