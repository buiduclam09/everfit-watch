package com.crazy_coder.everfit.data.remote.api

import com.crazy_coder.everfit.data.model.WorkoutResponse
import retrofit2.http.GET


interface WorkoutApi {
    @GET("workouts")
    suspend fun getWorkouts(): WorkoutResponse
}
