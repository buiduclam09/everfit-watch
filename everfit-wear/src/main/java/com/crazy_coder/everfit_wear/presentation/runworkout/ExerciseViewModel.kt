/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crazy_coder.everfit_wear.presentation.runworkout

import android.Manifest
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crazy_coder.everfit_wear.data.HealthServicesRepository
import com.crazy_coder.everfit_wear.data.ServiceState
import com.crazy_coder.everfit_wear.presentation.MainViewModel
import com.crazy_coder.everfit_wear.utils.Constants
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.function.BinaryOperator

/** Data class for the initial values we need to check before a user starts an exercise **/
data class ExerciseUiState(
    val hasExerciseCapabilities: Boolean = true,
    val isTrackingAnotherExercise: Boolean = false,
    val isShowRestTimer: Boolean = false
)

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {
    val _navigation = MutableSharedFlow<String>()
    val navigation = _navigation.asSharedFlow()
    private val _state = mutableStateOf(RunWorkoutState())
    val state: State<RunWorkoutState> = _state
    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val uiState: StateFlow<ExerciseUiState> = flow {
        emit(
            ExerciseUiState(
                hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
                isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000),
        ExerciseUiState()
    )


    private var _exerciseServiceState: MutableState<ServiceState> =
        healthServicesRepository.serviceState
    val exerciseServiceState = _exerciseServiceState

    init {
        viewModelScope.launch {
            healthServicesRepository.createService()
        }
    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun prepareExercise() = viewModelScope.launch { healthServicesRepository.prepareExercise() }
    fun startExercise() = viewModelScope.launch { healthServicesRepository.startExercise() }
    fun pauseExercise() = viewModelScope.launch { healthServicesRepository.pauseExercise() }
    fun endExercise() = viewModelScope.launch { healthServicesRepository.endExercise() }
    fun resumeExercise() = viewModelScope.launch { healthServicesRepository.resumeExercise() }
    fun isShowRestTimer() = false

    fun updateData(token: String) {
        _state.value = state.value.copy(token = token)
    }

    fun updateHeart(avgHeart: Int) {
        _state.value = state.value.copy(avgHeart = avgHeart)
    }

    fun  updateLaps(lap : Int){
        _state.value = state.value.copy(esclap = lap)
    }

    fun updateDistance(distance: String) {
        _state.value = state.value.copy(distance = distance)
    }

    fun updateCalories(calories: String) {
        _state.value = state.value.copy(calories = calories)
    }

    companion object {
        data class RunWorkoutState(
            val heartRate: String = "0.0",
            val avgHeart: Int = 0,
            val temperature: String = "0.0",
            val peace: String = "0",
            val distance: String = "0",
            val calories: String = "0",
            val esclap : Int = 0,
            val showButtonRequest: Boolean = true,
            val step: String = "0",
            val token: String = ""
        )
    }
}



