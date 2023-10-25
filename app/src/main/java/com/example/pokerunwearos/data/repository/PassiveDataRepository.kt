package com.example.pokerunwearos.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data")

class PassiveDataRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    private val dataStore = applicationContext.dataStore

    val stepsDaily: Flow<Long?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[STEPS_DAILY]
    }


    suspend fun setStepsDaily(stepsDaily: Long) {
        dataStore.edit { preferences ->
            preferences[STEPS_DAILY] = stepsDaily
        }
    }

    private companion object {
        val STEPS_DAILY = longPreferencesKey("steps_daily")

        const val TAG = "Passive Data Repo"
    }
}