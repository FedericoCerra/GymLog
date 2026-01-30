package com.example.learningkotlin.ui.components.homeScreenHelpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.ui.theme.HevyBlue
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutBar(
    activeWorkout: Workout?,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = activeWorkout != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        activeWorkout?.let { workout ->
            var workoutDurationSeconds by remember { mutableLongStateOf(0L) }

            LaunchedEffect(key1 = workout.isActive) {
                while (workout.isActive) {
                    val now = System.currentTimeMillis()
                    val start = workout.startTime ?: now
                    workoutDurationSeconds = (now - start) / 1000
                    delay(1000L)
                }
            }

            fun formatDuration(seconds: Long): String {
                val h = seconds / 3600
                val m = (seconds % 3600) / 60
                val s = seconds % 60
                return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HevyBlue)
                    .clickable { onClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlayArrow, 
                            contentDescription = null, 
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Active Workout",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = workout.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Text(
                        text = formatDuration(workoutDurationSeconds),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
