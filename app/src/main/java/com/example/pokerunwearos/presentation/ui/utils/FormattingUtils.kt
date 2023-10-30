package com.example.pokerunwearos.presentation.ui.utils

import android.text.style.RelativeSizeSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val UNITS_RELATIVE_SIZE = .6f
private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)
private val MILLIS_PER_SECOND = TimeUnit.SECONDS.toMillis(1)
private const val NO_MOVEMENT_PACE = 400_0140.0


fun formatNumberWithCommas(number: Long): String {
    val numberFormat = NumberFormat.getInstance(Locale.US)
    return numberFormat.format(number)
}

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
        val hundredthOfSecond = millis / 10
        append("%02d".format(hundredthOfSecond))
    }
}

fun formatCalories(
    calories: Double, hasUnit: Boolean = true
) = buildSpannedString {
    append(calories.roundToInt().toString())
    if (hasUnit) {
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append(" cal")
        }
    }
}

fun formatDistance(
    meters: Double,
    measurementUnit: MeasurementUnit = MeasurementUnit.METRIC,
    hasUnit: Boolean = true
) = buildSpannedString {
    val measurementConversionUnit = MeasurementMap[measurementUnit]!!

    append("%02.2f".format(meters / measurementConversionUnit.distanceConversion))
    if (hasUnit) {
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append(" ${measurementConversionUnit.distanceUnit}")
        }
    }
}

fun formatSpeed(
    metersPerSec: Double,
    measurementUnit: MeasurementUnit = MeasurementUnit.METRIC,
    hasUnit: Boolean = true
) = buildSpannedString {
    val measurementConversionUnit = MeasurementMap[measurementUnit]!!

    append("%02.1f".format(metersPerSec * measurementConversionUnit.speedConversion))
    if (hasUnit) {
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append(" ${measurementConversionUnit.speedUnit}")
        }
    }
}

fun formatPace(
    msPerKm: Double,
    measurementUnit: MeasurementUnit = MeasurementUnit.METRIC,
    hasUnit: Boolean = false,
    available: Boolean = true
) = buildSpannedString {
    val measurementConversionUnit = MeasurementMap[measurementUnit]!!

    if (msPerKm == Double.POSITIVE_INFINITY || msPerKm == NO_MOVEMENT_PACE) {
        if (available) append("__'__\"") else append("N/A")
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
        if (hasUnit) {
            inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
                append(" ${measurementConversionUnit.paceUnit}")
            }
        }
    }
}

sealed class ElapsedTime {
    class ElapsedTimeDuration(val duration: kotlin.time.Duration) : ElapsedTime()
    class ElapsedTimeLong(val long: Long) : ElapsedTime()
}