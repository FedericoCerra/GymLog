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
    workoutDuration: String,
    restTimerValue: String,
    restTimerColor: Color,
    onRestTimerClick: () -> Unit,
    volume: String,
    sets: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Workout Duration
        StatItem("Duration", workoutDuration)

        // 2. The Rest Timer (Clickable)
        StatItem(
            label = "Rest Timer",
            value = restTimerValue,
            valueColor = restTimerColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onRestTimerClick() }
                .padding(4.dp)
        )

        // 3. Volume
        StatItem("Volume", volume)

        // 4. Sets
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
            color = valueColor
        )
    }
}