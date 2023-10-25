package com.example.pokerunwearos.presentation.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun SummaryFormat(
    value: String, metric: String, modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.Center, modifier = modifier) {
            Text(
                textAlign = TextAlign.Center,
                text = value,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.secondary,
                fontSize = 24.sp
            )
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = modifier) {
            Text(
                textAlign = TextAlign.Center, text = metric, fontSize = 12.sp
            )
        }
    }

}

@Preview
@Composable
fun SummaryFormatPreview() {
    SummaryFormat(value = "5.3", metric = "km")
}