package com.example.learningkotlin.ui.components.historyScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learningkotlin.model.FinishedWorkout
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItem(
    workout: FinishedWorkout, 
    allHistory: List<FinishedWorkout>,
    onClick: () -> Unit, 
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(Date(workout.date))
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            // Header: User + Date + More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "User", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(text = dateStr, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                    }
                    
                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme.copy(surface = Color(0xFF1C1C1E)),
                        shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenu(
                            expanded = showMenu, 
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF1C1C1E))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Workout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { onDelete(); showMenu = false }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Workout Name
            Text(text = workout.name, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth()) {
                HistoryStatItem("Time", formatDuration(workout.durationSeconds), modifier = Modifier.weight(1f))
                HistoryStatItem("Volume", "${workout.totalVolume.toInt()} kg", modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Exercises Preview
            val previewLimit = 3
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                workout.exercises.take(previewLimit).forEach { exercise ->
                    val isPR = isPersonalRecord(exercise.name, exercise.sets.maxOfOrNull { it.weight } ?: 0.0, workout.date, allHistory)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val imagePath = exercise.imagePath
                            if (imagePath != null) {
                                val coilModel = if (imagePath.startsWith("http")) imagePath else "file:///android_asset/exercises/$imagePath"
                                AsyncImage(
                                    model = coilModel, 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                Icon(Icons.Default.FitnessCenter, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${exercise.sets.size} sets ${exercise.name}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isPR) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Star, 
                                                null, 
                                                tint = Color(0xFFFFD700), 
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                "PR", 
                                                color = Color(0xFFFFD700), 
                                                fontSize = 10.sp, 
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (workout.exercises.size > previewLimit) {
                    Text(
                        text = "And ${workout.exercises.size - previewLimit} more exercises",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 64.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = Color.DarkGray.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
        }
    }
}

private fun isPersonalRecord(
    exerciseName: String, 
    currentWeight: Double, 
    currentDate: Long, 
    allHistory: List<FinishedWorkout>
): Boolean {
    if (currentWeight <= 0) return false
    
    val previousMax = allHistory
        .filter { it.date < currentDate }
        .flatMap { it.exercises }
        .filter { it.name == exerciseName }
        .flatMap { it.sets }
        .maxOfOrNull { it.weight } ?: 0.0
        
    return currentWeight > previousMax
}

@Composable
fun HistoryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
