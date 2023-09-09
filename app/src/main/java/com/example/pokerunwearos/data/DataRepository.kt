package com.example.pokerunwearos.data

import com.example.pokerunwearos.model.Pokemon
import com.example.pokerunwearos.network.ApiService
import javax.inject.Inject

class DataRepository @Inject constructor(
    private val apiService: ApiService
) {
    /** Fetches list of MarsPhoto from marsApi*/
    suspend fun fetchData(): Pokemon {
        val minValue = 1
        val maxValue = 386
        val randomValue = (minValue..maxValue).random()

        return apiService.fetchData(randomValue.toString())
    }
}