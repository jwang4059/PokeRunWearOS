package com.example.pokerunwearos.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokerunwearos.data.models.Pokemon
import com.example.pokerunwearos.data.models.Workout
import com.example.pokerunwearos.data.repository.DataRepository
import com.example.pokerunwearos.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PostWorkoutViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    private val dataRepository: DataRepository,
) : ViewModel() {

    val uiState: StateFlow<Workout?> = workoutRepository.getLatestWorkout().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null
    )

    suspend fun fetchData(): Pokemon {
        return dataRepository.fetchData()
    }
}

sealed interface PokemonUiState {
    data class Success(val pokemon: Pokemon) : PokemonUiState
    object Error : PokemonUiState
    object Loading : PokemonUiState
}