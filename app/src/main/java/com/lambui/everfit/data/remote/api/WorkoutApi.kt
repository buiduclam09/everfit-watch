package com.lambui.everfit.data.remote.api

import com.lambui.everfit.data.model.WorkoutResponse
import retrofit2.http.GET


interface WorkoutApi {
    @GET("workouts")
    suspend fun getWorkouts(): WorkoutResponse
}
