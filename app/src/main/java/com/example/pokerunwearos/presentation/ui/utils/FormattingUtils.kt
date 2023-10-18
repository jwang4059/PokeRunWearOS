package com.example.pokerunwearos.presentation.ui.utils

import android.text.style.RelativeSizeSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val UNITS_RELATIVE_SIZE = .6f
private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)
private const val METERS_PER_KILOMETER = 1_000.0
private const val METERS_PER_MILE = 1_609.34
private const val MS_PER_KM_PER_SEC_PER_MILE = 621.371
private const val NO_MOVEMENT_PACE = 400_0140.0

fun formatElapsedTime(time: ElapsedTime, includeSeconds: Boolean) = buildSpannedString {
    val elapsedDuration = when (time) {
        is ElapsedTime.ElapsedTimeDuration -> time.duration
        is ElapsedTime.ElapsedTimeLong -> time.long.toDuration(DurationUnit.MILLISECONDS)
    }

    val hours = elapsedDuration.inWholeHours
    if (hours > 0) {
        append(hours.toString())
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append("h")
        }
    }
    val minutes = elapsedDuration.inWholeMinutes % MINUTES_PER_HOUR
    append("%02d".format(minutes))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append("m")
    }
    if (includeSeconds) {
        val seconds = elapsedDuration.inWholeSeconds % SECONDS_PER_MINUTE
        append("%02d".format(seconds))
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append("s")
        }
    }
}

fun formatCalories(calories: Double) = buildSpannedString {
    append(calories.roundToInt().toString())
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(" cal")
    }
}

fun formatDistance(meters: Double, measurementUnit: MeasurementUnit) = buildSpannedString {
    val conversionValue: Double
    val unit: String

    when (measurementUnit) {
        MeasurementUnit.METRIC -> {
            conversionValue = METERS_PER_KILOMETER
            unit = "km"
        }
        MeasurementUnit.IMPERIAL -> {
            conversionValue = METERS_PER_MILE
            unit = "mi"
        }
    }

    append("%02.2f".format(meters / conversionValue))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(unit)
    }
}

fun formatPaceMinPerMi(msPerKm: Double) = buildSpannedString {
    if (msPerKm == NO_MOVEMENT_PACE) {
        append("__'__\"")
    } else {
        val secPerMile = msPerKm / MS_PER_KM_PER_SEC_PER_MILE
        val minutes = secPerMile / SECONDS_PER_MINUTE
        append("%02.0f".format(minutes))
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append("'")
        }
        val seconds = secPerMile % SECONDS_PER_MINUTE
        append("%02.0f".format(seconds))
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append("\"")
        }
    }
}

sealed class ElapsedTime {
    class ElapsedTimeDuration(val duration: kotlin.time.Duration) : ElapsedTime()
    class ElapsedTimeLong(val long: Long) : ElapsedTime()
}

enum class MeasurementUnit {
    METRIC, // Representing metric units
    IMPERIAL // Representing imperial units
}