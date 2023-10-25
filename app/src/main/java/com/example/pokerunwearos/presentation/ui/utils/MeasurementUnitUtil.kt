package com.example.pokerunwearos.presentation.ui.utils

enum class MeasurementUnit {
    METRIC, // Representing metric units
    IMPERIAL // Representing imperial units
}

val MetricConversionUnits = MeasurementConversionUnit(
    distanceConversion = 1_000.0,
    paceConversion = 1000.0,
    speedConversion = 3.6,
    distanceUnit = " km",
    speedUnit = " kph"
)

val ImperialConversionUnits = MeasurementConversionUnit(
    distanceConversion = 1_609.34,
    paceConversion = 621.371,
    speedConversion = 2.23694,
    distanceUnit = " mi",
    speedUnit = " mph"
)

data class MeasurementConversionUnit(
    val distanceConversion: Double,
    val paceConversion: Double,
    val speedConversion: Double,
    val distanceUnit: String,
    val speedUnit: String,
)