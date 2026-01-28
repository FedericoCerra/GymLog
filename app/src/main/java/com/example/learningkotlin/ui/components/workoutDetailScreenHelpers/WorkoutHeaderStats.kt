package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkoutHeaderStats(
    mainTimerLabel: String, // "Est. Time" or "Duration"
    mainTimerValue: String,
    volumeLabel: String,    // "Total Volume" or "Volume"
    volumeValue: String,
    setsLabel: String,      // "Total Sets" or "Sets Done"
    setsValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Main Timer (Est. Time or Duration)
        StatItem(mainTimerLabel, mainTimerValue)

        // 2. Volume
        StatItem(volumeLabel, volumeValue)

        // 3. Sets
        StatItem(setsLabel, setsValue)
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}