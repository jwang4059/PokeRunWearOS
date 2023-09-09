package com.example.pokerunwearos.ui

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseGoalType
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokerunwearos.data.DataRepository
import com.example.pokerunwearos.data.HealthServicesRepository
import com.example.pokerunwearos.data.ServiceState
import com.example.pokerunwearos.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseUiState(
    val exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = mutableMapOf(),
    val isTrackingAnotherExercise: Boolean = false,
)

data class PokeRunUiState(
    val currentExerciseType: ExerciseType = ExerciseType.RUNNING,
    val currentExerciseGoal: ExerciseGoal<Double>? = null,
)

@HiltViewModel
class PokeRunViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
    private val dataRepository: DataRepository
) : ViewModel() {

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val exerciseUiState: StateFlow<ExerciseUiState> = flow {
        emit(
            ExerciseUiState(
                exerciseCapabilities = healthServicesRepository.getExerciseCapabilities(),
                isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            )
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(3_000), ExerciseUiState()
    )

    private val _appUiState = MutableStateFlow(PokeRunUiState())
    val appUiState: StateFlow<PokeRunUiState> = _appUiState.asStateFlow()

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

    fun hasExerciseCapabilities(
        capabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>?,
        exerciseType: ExerciseType? = null
    ): Boolean {
        return if (exerciseType != null) capabilities?.get(exerciseType) != null else capabilities != null
    }

    fun setExercise(exerciseType: ExerciseType) {
        _appUiState.update { currentState -> currentState.copy(currentExerciseType = exerciseType) }
    }

    fun setExerciseGoal(distanceThreshold: Double) {
        _appUiState.update { currentState ->
            currentState.copy(
                currentExerciseGoal = ExerciseGoal.createOneTimeGoal(
                    DataTypeCondition(
                        dataType = DataType.DISTANCE_TOTAL,
                        threshold = distanceThreshold,
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    )
                )
            )
        }
    }

    fun supportsGoalType(
        capabilities: ExerciseTypeCapabilities?,
        exerciseGoalType: ExerciseGoalType?,
        dataType: DataType<*, *>
    ) = healthServicesRepository.supportsGoalType(capabilities, exerciseGoalType, dataType)

    fun prepareExercise() = viewModelScope.launch {
        healthServicesRepository.prepareExercise(
            appUiState.value.currentExerciseType
        )
    }

    fun startExercise() = viewModelScope.launch {
        healthServicesRepository.startExercise(
            appUiState.value.currentExerciseType, appUiState.value.currentExerciseGoal
        )
    }

    fun pauseExercise() = viewModelScope.launch { healthServicesRepository.pauseExercise() }
    fun endExercise() = viewModelScope.launch { healthServicesRepository.endExercise() }
    fun resumeExercise() = viewModelScope.launch { healthServicesRepository.resumeExercise() }

    suspend fun fetchData(): Pokemon {
        return dataRepository.fetchData()
    }
}

