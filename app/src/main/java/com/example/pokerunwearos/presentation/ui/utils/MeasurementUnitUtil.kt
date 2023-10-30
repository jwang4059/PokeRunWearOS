package com.example.pokerunwearos.presentation.ui.utils

enum class MeasurementUnit {
    METRIC, // Representing metric units
    IMPERIAL // Representing imperial units
}

data class MeasurementConversionUnit(
    val distanceConversion: Double,
    val paceConversion: Double,
    val speedConversion: Double,
    val distanceUnit: String,
    val paceUnit: String,
    val speedUnit: String,
    val stepsUnit: String = "steps",
    val energyUnit: String = "cal",
    val hrUnit: String = "bpm",
)

val MetricConversionUnits = MeasurementConversionUnit(
    distanceConversion = 1_000.0,
    paceConversion = 1000.0,
    speedConversion = 3.6,
    distanceUnit = "km",
    paceUnit = "/km",
    speedUnit = "kph"
)

val ImperialConversionUnits = MeasurementConversionUnit(
    distanceConversion = 1_609.34,
    paceConversion = 621.371,
    speedConversion = 2.23694,
    distanceUnit = "mi",
    paceUnit = "/mi",
    speedUnit = "mph"
)

val MeasurementMap = mapOf(
    MeasurementUnit.METRIC to MetricConversionUnits,
    MeasurementUnit.IMPERIAL to ImperialConversionUnits
)

