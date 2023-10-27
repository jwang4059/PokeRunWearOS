package com.example.pokerunwearos.data.models

import androidx.health.services.client.data.ExerciseType
import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromExerciseType(exerciseType: ExerciseType?): Int? {
        return exerciseType?.hashCode()
    }

    @TypeConverter
    fun intToExerciseType(id: Int?): ExerciseType? {
        return id?.let { ExerciseType.fromId(it) }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}