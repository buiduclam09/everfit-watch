package com.crazy_coder.everfit_wear.presentation

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crazy_coder.everfit_wear.data.PassiveDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PassiveDataRepository,
) : ViewModel() {
    private val _state = mutableStateOf(MainState())
    val state: State<MainState> = _state
    var permissionLauncher: ActivityResultLauncher<String>? = null

    init {
        repository.latestHeartRate
            .onEach { _state.value = state.value.copy(heartRate = it.toString()) }
            .launchIn(viewModelScope)
        repository.latestTemperature
            .onEach { _state.value = state.value.copy(temperature = it.toString()) }
            .launchIn(viewModelScope)
        repository.latestDistances
            .onEach {
                _state.value = state.value.copy(step = it.toString())
            }.launchIn(viewModelScope)
    }

    fun updateStatusPermission(isGrant: Boolean) {
        _state.value = state.value.copy(showButtonRequest = !isGrant)
    }

    fun updateToken(token: String) {
        _state.value = state.value.copy(token = token)
    }

    companion object {
        data class MainState(
            val heartRate: String = "0.0",
            val temperature: String = "0.0",
            val showButtonRequest: Boolean = true,
            val step: String = "0",
            val token: String = ""
        )
    }
}