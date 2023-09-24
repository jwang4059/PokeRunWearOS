package com.example.pokerunwearos.presentation.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.health.services.client.data.DataTypeAvailability
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.pokerunwearos.R

@Composable
fun HeartRateLabel(
    hrBPM: Double,
    availability: DataTypeAvailability
) {
    val text = if (availability == DataTypeAvailability.AVAILABLE && hrBPM > 0) {
        hrBPM.toInt().toString()
    } else {
        stringResource(id = R.string.no_hr_reading)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = stringResource(R.string.icon),
            tint = Color.Red
        )
        Text(
            text = text,
            style = MaterialTheme.typography.display1
        )
    }
}
