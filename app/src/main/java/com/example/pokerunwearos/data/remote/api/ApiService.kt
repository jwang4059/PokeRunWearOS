package com.example.pokerunwearos.data.remote.api

import com.example.pokerunwearos.data.models.Pokemon
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("pokemon/{pokemonId}")
    suspend fun fetchData(@Path("pokemonId") pokemonId: String): Pokemon
}