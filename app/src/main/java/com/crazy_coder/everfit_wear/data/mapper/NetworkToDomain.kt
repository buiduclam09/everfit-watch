package com.crazy_coder.everfit_wear.data.mapper

import com.crazy_coder.everfit_wear.data.model.Room
import com.crazy_coder.everfit_wear.data.model.User
import com.crazy_coder.everfit_wear.data.response.RoomResponse
import com.crazy_coder.everfit_wear.data.response.UserResponse

fun RoomResponse.mapToRoom(): Room = Room(
    id, name, facility, building,
)

fun UserResponse.mapToUser(): User =
    User(id, userName, supervisor, objectID, realName, facility, building)