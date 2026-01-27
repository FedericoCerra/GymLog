package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RestTimerChip(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Helper to format text inside the chip
    fun formatRestTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }

    Surface(
        onClick = { showMenu = true },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatRestTime(currentSeconds),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }

        // The Dropdown Logic
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).width(160.dp)
        ) {
            val times = listOf(30 to "30s", 60 to "1m", 90 to "1m 30s", 120 to "2m", 180 to "3m", 300 to "5m")
            times.forEach { (seconds, label) ->
                val isSelected = (seconds == currentSeconds)
                DropdownMenuItem(
                    text = {
                        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    },
                    trailingIcon = { if (isSelected) Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary) },
                    onClick = { onTimeSelected(seconds); showMenu = false }
                )
            }
        }
    }
}