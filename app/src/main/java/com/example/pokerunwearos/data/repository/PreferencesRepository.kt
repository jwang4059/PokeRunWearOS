package com.example.pokerunwearos.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    private val dataStore = applicationContext.dataStore

    val exerciseType: Flow<String?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[EXERCISE_TYPE]
    }

    val exerciseGoal: Flow<Double?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[EXERCISE_GOAL]
    }


    suspend fun setExerciseType(exerciseType: String) {
        dataStore.edit { preferences ->
            preferences[EXERCISE_TYPE] = exerciseType
        }
    }

    suspend fun setExerciseGoal(exerciseGoal: Double) {
        dataStore.edit { preferences ->
            preferences[EXERCISE_GOAL] = exerciseGoal
        }
    }

    private companion object {
        val EXERCISE_TYPE = stringPreferencesKey("exercise_type")
        val EXERCISE_GOAL = doublePreferencesKey("exercise_goal")

        const val TAG = "Preferences Repo"
    }
}