package com.crazy_coder.everfit_wear.data.response

data class ListRoomResponse (
    val values: List<RoomResponse>
)

data class RoomResponse (
    val id: Long,
    val name: String,
    val facility: Long,
    val building: Long
)