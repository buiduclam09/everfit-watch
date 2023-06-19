package com.crazy_coder.everfit_wear.data.response


data class ListUserResponse (
    val values: List<UserResponse>
)
data class UserResponse(
    val id: Long,
    val userName: String,
    val supervisor: Boolean,
    val objectID: Any? = null,
    val realName: String,
    val facility: Long,
    val building: Long
)