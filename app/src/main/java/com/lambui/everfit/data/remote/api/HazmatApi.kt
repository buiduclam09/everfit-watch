package com.lambui.everfit.data.remote.api

import com.lambui.everfit.data.response.ListRoomResponse
import com.lambui.everfit.data.response.ListUserResponse
import com.lambui.everfit.data.response.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HazmatApi {
    @GET("/api/UserProfile/login/{username}/{password}")
    suspend fun login(@Path("username") username: String, @Path("password") password: String): String

    @GET("api/Room")
    suspend fun getAllRoom(): ListRoomResponse

    @GET("/api/UserProfile/{id}")
    suspend fun getUserProfile(@Path("id") id: String): UserResponse

    @GET("/api/UserProfile")
    suspend fun getAllUsersActive(): ListUserResponse


}