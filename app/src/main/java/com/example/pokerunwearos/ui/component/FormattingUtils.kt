package com.example.pokerunwearos.ui.component

import android.text.style.RelativeSizeSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val UNITS_RELATIVE_SIZE = .6f
private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)
private const val METERS_PER_MILE = 1_609.34
private const val MS_PER_KM_PER_SEC_PER_MILE = 621.371
private const val NO_MOVEMENT_PACE = 400_0140.0

fun formatElapsedTime(elapsedDuration: kotlin.time.Duration, includeSeconds: Boolean) =
    buildSpannedString {
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

fun formatDistanceKm(meters: Double) = buildSpannedString {
    append("%02.2f".format(meters / 1_000))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append("km")
    }
}

fun formatDistanceMi(meters: Double) = buildSpannedString {
    append("%02.2f".format(meters / METERS_PER_MILE))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append("mi")
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