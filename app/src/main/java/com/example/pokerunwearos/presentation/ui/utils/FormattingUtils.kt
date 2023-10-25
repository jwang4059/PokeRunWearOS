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
private val MILLIS_PER_SECOND = TimeUnit.SECONDS.toMillis(1)
private const val NO_MOVEMENT_PACE = 400_0140.0

fun formatElapsedTime(
    time: ElapsedTime, includeSeconds: Boolean = true, includeHundredth: Boolean = false
) = buildSpannedString {
    val elapsedDuration = when (time) {
        is ElapsedTime.ElapsedTimeDuration -> time.duration
        is ElapsedTime.ElapsedTimeLong -> time.long.toDuration(DurationUnit.MILLISECONDS)
    }

    val hours = elapsedDuration.inWholeHours
    if (hours > 0) {
        append(hours.toString())
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append(":")
        }
    }
    val minutes = elapsedDuration.inWholeMinutes % MINUTES_PER_HOUR
    append("%02d".format(minutes))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(":")
    }
    if (includeSeconds) {
        val seconds = elapsedDuration.inWholeSeconds % SECONDS_PER_MINUTE
        append("%02d".format(seconds))
        if (includeHundredth) {
            inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
                append(":")
            }
        }
    }
    if (includeHundredth) {
        val millis = elapsedDuration.inWholeMilliseconds % MILLIS_PER_SECOND
        val hundredth_of_second = millis / 10
        append("%02d".format(hundredth_of_second))
    }
}

fun formatCalories(calories: Double) = buildSpannedString {
    append(calories.roundToInt().toString())
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(" cal")
    }
}

fun formatDistance(meters: Double, measurementUnit: MeasurementUnit) = buildSpannedString {
    val measurementConversionUnit =
        if (measurementUnit == MeasurementUnit.METRIC) MetricConversionUnits else ImperialConversionUnits

    append("%02.2f".format(meters / measurementConversionUnit.distanceConversion))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(measurementConversionUnit.distanceUnit)
    }
}

fun formatPace(msPerKm: Double, measurementUnit: MeasurementUnit) = buildSpannedString {
    val measurementConversionUnit =
        if (measurementUnit == MeasurementUnit.METRIC) MetricConversionUnits else ImperialConversionUnits

    if (msPerKm == Double.POSITIVE_INFINITY || msPerKm == NO_MOVEMENT_PACE) {
        append("__'__\"")
    } else {
        val secPerMile = msPerKm / measurementConversionUnit.paceConversion
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

fun formatSpeed(metersPerSec: Double, measurementUnit: MeasurementUnit) = buildSpannedString {
    val measurementConversionUnit =
        if (measurementUnit == MeasurementUnit.METRIC) MetricConversionUnits else ImperialConversionUnits

    append("%02.1f".format(metersPerSec * measurementConversionUnit.speedConversion))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(measurementConversionUnit.speedUnit)
    }
}

sealed class ElapsedTime {
    class ElapsedTimeDuration(val duration: kotlin.time.Duration) : ElapsedTime()
    class ElapsedTimeLong(val long: Long) : ElapsedTime()
}