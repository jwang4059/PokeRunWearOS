package com.example.pokerunwearos.data.repository

import com.example.pokerunwearos.data.local.dao.WorkoutDao
import com.example.pokerunwearos.data.models.Workout
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutRepository @Inject constructor(private val workoutDao: WorkoutDao) {
    fun getLatestWorkout(): Flow<Workout?> = workoutDao.getLatestWorkout()

    suspend fun insertWorkout(workout: Workout) = workoutDao.insertWorkout(workout)

    suspend fun updateWorkout(workout: Workout) = workoutDao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: Workout) = workoutDao.deleteWorkout(workout)
}