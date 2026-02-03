package com.federicocerra.gymlog.ui.components.historyScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.federicocerra.gymlog.model.FinishedWorkout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
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

    // Identify all PRs in this session
    val sessionPRs = remember(workout) {
        workout.exercises.flatMap { ex ->
            val prSets = ex.sets.filter { it.isWeightPR || it.is1RMPR || it.isVolumePR }
            prSets.map { set -> 
                val types = mutableListOf<String>()
                if (set.isWeightPR) types.add("Weight")
                if (set.is1RMPR) types.add("1RM")
                if (set.isVolumePR) types.add("Volume")
                ex.name to types
            }
        }.groupBy({ it.first }, { it.second }).mapValues { entry -> 
            entry.value.flatten().distinct() 
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
            // 1. Header: User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMenu, 
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Workout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // 2. Workout Name
            Text(
                text = workout.name, 
                fontWeight = FontWeight.Black, 
                fontSize = 22.sp, 
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 3. Stats Summary
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryStatItem("Time", formatDuration(workout.durationSeconds), modifier = Modifier.weight(1f))
                HistoryStatItem("Volume", "${workout.totalVolume.toInt()} kg", modifier = Modifier.weight(1f))
            }

            // 4. Personal Records Summary (If any)
            if (sessionPRs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PERSONAL RECORDS", 
                            color = Color(0xFFFFD700), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sessionPRs.forEach { (exerciseName, types) ->
                            PRChip(exerciseName, types)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 5. Exercises Preview
            val previewLimit = 3
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                workout.exercises.take(previewLimit).forEach { exercise ->
                    val hasAnyPR = exercise.sets.any { it.isWeightPR || it.is1RMPR || it.isVolumePR }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
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
                                Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${exercise.sets.size} sets ${exercise.name}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (hasAnyPR) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.WorkspacePremium, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PRChip(exerciseName: String, types: List<String>) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exerciseName, 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            types.forEach { type ->
                val (icon, color) = when(type) {
                    "Weight" -> Icons.Default.Star to Color(0xFFFFD700)
                    "1RM" -> Icons.Default.ElectricBolt to Color(0xFF00E5FF)
                    else -> Icons.Default.TrendingUp to Color(0xFF76FF03)
                }
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = color, 
                    modifier = Modifier.size(10.dp).padding(horizontal = 1.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label.uppercase(), 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 9.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 0.5.sp
        )
        Text(
            text = value, 
            color = MaterialTheme.colorScheme.onSurface,
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
