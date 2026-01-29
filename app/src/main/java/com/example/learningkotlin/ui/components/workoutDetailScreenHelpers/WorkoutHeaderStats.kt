package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learningkotlin.ui.components.common.StatCard

@Composable
fun WorkoutHeaderStats(
    mainTimerLabel: String,
    mainTimerValue: String,
    volumeLabel: String,
    volumeValue: String,
    setsLabel: String,
    setsValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = mainTimerLabel,
            value = mainTimerValue,
            icon = Icons.Default.Timer,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = volumeLabel,
            value = volumeValue,
            icon = Icons.Default.FitnessCenter,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = setsLabel,
            value = setsValue,
            icon = Icons.Default.Layers,
            modifier = Modifier.weight(1f)
        )
    }
}
