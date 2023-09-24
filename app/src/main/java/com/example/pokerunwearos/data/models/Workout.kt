package com.example.pokerunwearos.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "workout")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_type") val exerciseType: String,
    @ColumnInfo(name = "time_millis") val timeMillis: Long = 0,
    @ColumnInfo(name = "distance") val distance: Double = 0.0,
    @ColumnInfo(name = "pace") val pace: Double = 0.0,
    @ColumnInfo(name = "steps") val steps: Long = 0,
    @ColumnInfo(name = "calories") val calories: Double = 0.0,
    @ColumnInfo(name = "avg_heart_rate") val avgHeartRate: Double = 0.0,
    @ColumnInfo(name = "distance_goal") val distanceGoal: Double? = null,
    @ColumnInfo(name = "date") val date: Date = Date(),
)