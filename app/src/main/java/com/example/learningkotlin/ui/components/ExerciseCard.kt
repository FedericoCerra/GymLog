package com.example.learningkotlin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.WorkoutSet

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onStartTimer: (Int) -> Unit
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Logic to format "90" -> "1m 30s"
    fun formatRestTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Title Row with New Timer Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // A. Exercise Name
                Text(
                    text = exercise.name,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )


                // B. THE NEW TIMER CHIP CALL
                RestTimerChip(
                    currentTimeText = formatRestTime(exercise.restTimer),
                    currentSeconds = exercise.restTimer, // <--- ADD THIS LINE
                    onTimeSelected = { newTime ->
                        exercise.restTimer = newTime
                        refreshTrigger++
                    }
                )
            }

            // Options Icon (Right side)
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Table Header
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            HeaderLabel("SET", Modifier.weight(1f))
            HeaderLabel("PREVIOUS", Modifier.weight(2f))
            HeaderLabel("KG", Modifier.weight(1.5f))
            HeaderLabel("REPS", Modifier.weight(1.5f))
            Spacer(modifier = Modifier.weight(1f)) // Checkbox space
        }

        // 3. The Sets Loop
        key(refreshTrigger) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                exercise.sets.forEachIndexed { index, set ->
                    SetRow(
                        index = index + 1,
                        set = set,
                        onDelete = {
                            exercise.sets.remove(set)
                            refreshTrigger++
                        },
                        onCheck = { isChecked ->
                            if (isChecked) {
                                onStartTimer(exercise.restTimer)
                            }
                        }
                    )
                }
            }
        }

        // 4. Add Set Button
        Button(
            onClick = {
                val lastSet = exercise.sets.lastOrNull()
                val newWeight = lastSet?.weight ?: 0.0
                val newReps = lastSet?.reps ?: 0
                val nextId = (exercise.sets.maxOfOrNull { it.id } ?: 0) + 1

                exercise.sets.add(WorkoutSet(nextId, newWeight, newReps, false))
                refreshTrigger++
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+ Add set")
        }
    }
}
@Composable
private fun RestTimerChip(
    currentTimeText: String,
    currentSeconds: Int, // Pass the actual number (90, 120) to know what to highlight
    onTimeSelected: (Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // The Chip UI (The button itself)
    Surface(
        onClick = { showMenu = true },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface, // Darker background for contrast
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.height(32.dp) // Slightly taller for easier tapping
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications, // <--- THE CORRECT CLOCK ICON
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, // Blue tint
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = currentTimeText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        // The Dropdown Menu (Styled Better)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant) // Dark Grey background
                .width(160.dp) // Fixed width for consistency
        ) {
            val times = listOf(
                30 to "30s",
                60 to "1m",
                90 to "1m 30s",
                120 to "2m",
                180 to "3m",
                300 to "5m"
            )

            times.forEach { (seconds, label) ->
                val isSelected = (seconds == currentSeconds)

                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = {
                        onTimeSelected(seconds)
                        showMenu = false
                    }
                )
            }
        }
    }
}

// Keep your existing HeaderLabel helper
@Composable
private fun HeaderLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}