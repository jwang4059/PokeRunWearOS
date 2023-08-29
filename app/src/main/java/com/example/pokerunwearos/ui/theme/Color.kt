package com.example.pokerunwearos.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val md_theme_dark_primary = Color(0xFFFFCC80)
val md_theme_dark_secondary = Color(0xFFFF9800)
val md_theme_dark_background = Color(0xFF64B5F6)

internal val wearColorPalette: Colors = Colors(
    primary = md_theme_dark_primary,
    primaryVariant = Color.Gray,
    error = Color.Red,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onError = Color.Black
)