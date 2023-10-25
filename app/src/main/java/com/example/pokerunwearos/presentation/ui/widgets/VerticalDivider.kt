package com.example.pokerunwearos.presentation.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    height: Float = 1f,
    width: Dp = 1.dp,
    color: Color = Color.White
) {
    Spacer(
        modifier = modifier
            .fillMaxHeight(height)
            .width(width)
            .background(color)
    )
}