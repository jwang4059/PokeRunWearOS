package com.example.pokerunwearos.ui

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokerunwearos.data.HealthServicesRepository
import com.example.pokerunwearos.data.ServiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokeRunUiState(
    val hasExerciseCapabilities: Boolean = true,
    val isTrackingAnotherExercise: Boolean = false,
)

@HiltViewModel
class PokeRunViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val uiState: StateFlow<PokeRunUiState> = flow {
        emit(
            PokeRunUiState(
                hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
                isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000),
        PokeRunUiState()
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
}