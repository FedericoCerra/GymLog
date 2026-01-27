package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkoutHeaderStats(
    timerValue: String,        // Changed from 'duration' to be dynamic
    timerColor: Color,         // To show Blue when running
    onTimerClick: () -> Unit,  // The click action
    volume: String,
    sets: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. The Timer (Clickable)
        StatItem(
            label = "Rest Timer", // Renamed for clarity
            value = timerValue,
            valueColor = timerColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onTimerClick() } // <--- The magic click listener
                .padding(4.dp) // Touch target padding
        )

        // 2. Volume
        StatItem("Volume", volume)

        // 3. Sets
        StatItem("Sets", sets)
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
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor // Allows the timer to turn blue
        )
    }
}