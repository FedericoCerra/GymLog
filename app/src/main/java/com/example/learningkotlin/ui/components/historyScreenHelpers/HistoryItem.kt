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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learningkotlin.model.FinishedWorkout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItem(
    workout: FinishedWorkout, 
    allHistory: List<FinishedWorkout>,
    onClick: () -> Unit, 
    onDelete: () -> Unit
) {
    val user = remember { FirebaseAuth.getInstance().currentUser }
    val displayName = user?.displayName ?: user?.email?.split("@")?.firstOrNull() ?: "Athlete"
    val photoUrl = user?.photoUrl
    
    val dateStr = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(Date(workout.date))
    var showMenu by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        // Reduced start padding from 14dp to 8dp to move icon further left
        Column(modifier = Modifier.padding(start = 8.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
            // Header: Actual User + Date + More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Actual User Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f))
                            .border(1.dp, primaryColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl.toString(),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = displayName.take(1).uppercase(),
                                color = primaryColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayName, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 15.sp, 
                            color = Color.White
                        )
                        Text(
                            text = dateStr, 
                            color = Color.Gray, 
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
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
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Workout Name - Indented slightly more to align with the text in the header
            Text(
                text = workout.name, 
                fontWeight = FontWeight.Black, 
                fontSize = 22.sp, 
                color = Color.White,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row with Glass Pills
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryStatItem("Time", formatDuration(workout.durationSeconds), modifier = Modifier.weight(1f))
                HistoryStatItem("Volume", "${workout.totalVolume.toInt()} kg", modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Exercises Preview
            val previewLimit = 3
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                workout.exercises.take(previewLimit).forEach { exercise ->
                    val isPR = isPersonalRecord(exercise.name, exercise.sets.maxOfOrNull { it.weight } ?: 0.0, workout.date, allHistory)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val imagePath = exercise.imagePath
                            if (imagePath != null) {
                                val coilModel = if (imagePath.startsWith("http")) imagePath else "file:///android_asset/exercises/$imagePath"
                                AsyncImage(
                                    model = coilModel, 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.FitnessCenter, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${exercise.sets.size} sets ${exercise.name}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isPR) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                                                fontSize = 9.sp, 
                                                fontWeight = FontWeight.Black
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
                        text = "+ ${workout.exercises.size - previewLimit} more exercises",
                        color = primaryColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 58.dp)
                    )
                }
            }
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label.uppercase(), 
            color = Color.Gray, 
            fontSize = 9.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 0.5.sp
        )
        Text(
            text = value, 
            color = Color.White, 
            fontSize = 16.sp, 
            fontWeight = FontWeight.Black
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
