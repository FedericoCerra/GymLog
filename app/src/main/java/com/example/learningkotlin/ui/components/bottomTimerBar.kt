package com.example.learningkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomTimerBar(
    secondsRemaining: Int,
    onSkip: () -> Unit,
    onAdd15: () -> Unit,
    onSub15: () -> Unit
) {
    // Format: 01:30
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    // A Floating "Pill" Design
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(70.dp)
            .shadow(12.dp, RoundedCornerShape(35.dp)), // Soft shadow
        shape = RoundedCornerShape(35.dp), // Pill shape
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Dark Grey
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp), // Tight padding for the pill look
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. THE TIME (Blue Circle)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace // Keeps numbers stable
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. TEXT STATUS
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Resting...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Small progress bar visualization could go here later
            }

            // 3. CONTROLS (Row of circles)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // -15s
                TimerCircleButton(text = "-15") { onSub15() }

                Spacer(modifier = Modifier.width(8.dp))

                // +15s
                TimerCircleButton(text = "+15") { onAdd15() }

                Spacer(modifier = Modifier.width(12.dp))

                // Close (X)
                IconButton(onClick = onSkip) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TimerCircleButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background) // Black background for contrast
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}