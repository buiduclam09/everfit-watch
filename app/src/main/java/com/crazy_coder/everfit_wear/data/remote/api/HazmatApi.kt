package com.crazy_coder.everfit_wear.data.remote.api

import com.crazy_coder.everfit_wear.data.response.ListRoomResponse
import com.crazy_coder.everfit_wear.data.response.ListUserResponse
import com.crazy_coder.everfit_wear.data.response.UserResponse
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