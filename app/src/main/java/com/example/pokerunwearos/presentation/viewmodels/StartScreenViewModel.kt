package com.example.pokerunwearos.presentation.viewmodels

import android.Manifest
import androidx.health.services.client.data.DataTypeAvailability
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokerunwearos.data.repository.health.HealthServicesRepository
import com.example.pokerunwearos.data.repository.health.MeasureMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
) : ViewModel() {

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION
    )

    private val _uiState: MutableStateFlow<HeartRateUiState> = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val supported = healthServicesRepository.hasHeartRateCapability()
            _uiState.update { currentState -> currentState.copy(heartRateSupported = supported) }
        }

        viewModelScope.launch {
            healthServicesRepository.heartRateMeasureFlow().collect { measureMessage ->
                when (measureMessage) {
                    is MeasureMessage.MeasureAvailability -> {
                        _uiState.update { currentState -> currentState.copy(heartRateAvailable = measureMessage.availability) }
                    }

                    is MeasureMessage.MeasureData -> {
                        _uiState.update { currentState -> currentState.copy(heartRateBpm = measureMessage.data.last().value) }
                    }
                }
            }
        }
    }
}

data class HeartRateUiState(
    val heartRateSupported: Boolean = true,
    val heartRateAvailable: DataTypeAvailability = DataTypeAvailability.UNKNOWN,
    val heartRateBpm: Double = 0.0
)
