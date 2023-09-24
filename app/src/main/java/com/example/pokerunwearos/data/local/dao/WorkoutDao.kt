package com.example.pokerunwearos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pokerunwearos.data.models.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout ORDER BY id DESC LIMIT 1")
    fun getLatestWorkout(): Flow<Workout?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)


}