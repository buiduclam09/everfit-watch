package com.lambui.everfit.data.mapper

import com.lambui.everfit.data.model.Room
import com.lambui.everfit.data.model.User
import com.lambui.everfit.data.response.RoomResponse
import com.lambui.everfit.data.response.UserResponse

fun RoomResponse.mapToRoom(): Room = Room(
    id, name, facility, building,
)

fun UserResponse.mapToUser(): User =
    User(id, userName, supervisor, objectID, realName, facility, building)