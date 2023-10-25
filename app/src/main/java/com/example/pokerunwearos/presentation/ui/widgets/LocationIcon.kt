package com.example.pokerunwearos.presentation.ui.widgets

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.LocationAvailability
import androidx.wear.compose.material.Icon

@Composable
fun LocationIcon(locationAvailability: LocationAvailability) {
    val hasGPS =
        locationAvailability == LocationAvailability.ACQUIRED_TETHERED || locationAvailability == LocationAvailability.ACQUIRED_UNTETHERED

    if (hasGPS) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location On",
            tint = Color.Green,
            modifier = Modifier
                .size(28.dp)
                .padding(4.dp)
        )
    } else {
        val infiniteTransition = rememberInfiniteTransition()
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = 0F,
            targetValue = 1F,
            animationSpec = infiniteRepeatable(
                animation = tween(500), repeatMode = RepeatMode.Reverse
            ),
        )

        Icon(imageVector = Icons.Default.LocationOn,
            contentDescription = "Location On",
            tint = Color.Green,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    alpha = animatedAlpha
                }
                .padding(4.dp))
    }
}