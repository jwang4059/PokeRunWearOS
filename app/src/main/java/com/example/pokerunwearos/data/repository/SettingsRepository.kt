package com.example.pokerunwearos.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository @Inject constructor(
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

    val dailyStepsGoal: Flow<Int?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[DAILY_STEPS_GOAL]
    }

    val skipPrompt: Flow<Boolean?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[SKIP_PROMPT]
    }

    val autoPause: Flow<Boolean?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[AUTO_PAUSE]
    }

    val gender: Flow<String?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[GENDER]
    }

    val language: Flow<String?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[LANGUAGE]
    }

    val useMetric: Flow<Boolean?> = dataStore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[USE_METRIC]
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

    suspend fun setDailyStepsGoal(dailyStepsGoal: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_STEPS_GOAL] = dailyStepsGoal
        }
    }

    suspend fun setSkipPrompt(skipPrompt: Boolean) {
        dataStore.edit { preferences ->
            preferences[SKIP_PROMPT] = skipPrompt
        }
    }

    suspend fun setAutoPause(autoPause: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PAUSE] = autoPause
        }
    }

    suspend fun setGender(gender: String) {
        dataStore.edit { preferences ->
            preferences[GENDER] = gender
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    suspend fun setUseMetric(useMetric: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_METRIC] = useMetric
        }
    }

    private companion object {
        val EXERCISE_TYPE = stringPreferencesKey("exercise_type")
        val EXERCISE_GOAL = doublePreferencesKey("exercise_goal")
        val DAILY_STEPS_GOAL = intPreferencesKey("daily_steps_goal")
        val SKIP_PROMPT = booleanPreferencesKey("skip_prompt")
        val AUTO_PAUSE = booleanPreferencesKey("auto_pause")
        val GENDER = stringPreferencesKey("gender")
        val LANGUAGE = stringPreferencesKey("language")
        val USE_METRIC = booleanPreferencesKey("use_metric")

        const val TAG = "Settings Repo"
    }
}