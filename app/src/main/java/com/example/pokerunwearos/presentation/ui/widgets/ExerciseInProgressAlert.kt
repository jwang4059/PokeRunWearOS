package com.example.pokerunwearos.presentation.ui.widgets

import android.app.Activity
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.example.pokerunwearos.R

@Composable
fun ExerciseInProgressAlert(isTrackingExercise: Boolean) {
    val showDialog = remember { mutableStateOf(isTrackingExercise) }
    val context = LocalContext.current
    Dialog(showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false }) {
        Alert(
            title = {
                Text(
                    stringResource(id = R.string.exercise_in_progress),
                    textAlign = TextAlign.Center
                )
            },
            negativeButton = {
                Button(
                    onClick = { (context as? Activity)?.finish() },
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.deny)
                    )

                }
            },
            positiveButton = {
                Button(
                    onClick = { showDialog.value = false },
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                ) {
                    Icon(
                        Icons.Default.Check, contentDescription = stringResource(
                            id = R.string.confirm
                        )
                    )
                }
            }
        ) {
            Text(
                text = stringResource(id = R.string.ending_continue),
                textAlign = TextAlign.Center
            )

        }
    }
}