package com.example.pokerunwearos.network

import com.example.pokerunwearos.model.Pokemon
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("pokemon/{pokemonId}")
    suspend fun fetchData(@Path("pokemonId") pokemonId: String): Pokemon
}