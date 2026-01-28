package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.theme.HevyBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    history: List<FinishedWorkout>,
    onWorkoutClick: (FinishedWorkout) -> Unit,
    onDeleteWorkout: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workouts recorded yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. COMPREHENSIVE ANALYSIS SECTION
                item {
                    HistoryDashboard(history)
                }

                // 2. RECENT WORKOUTS TITLE
                item {
                    Text(
                        text = "Recent Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 3. HISTORY ITEMS
                items(history, key = { it.id }) { workout ->
                    HistoryItem(
                        workout = workout, 
                        onClick = { onWorkoutClick(workout) },
                        onDelete = { onDeleteWorkout(workout.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun HistoryDashboard(history: List<FinishedWorkout>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // A. QUICK STATS ROW
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val totalWorkouts = history.size
            val totalVolume = history.sumOf { it.totalVolume }.toInt()
            val totalDurationHours = history.sumOf { it.durationSeconds } / 3600

            item { QuickStatCard("Total Workouts", totalWorkouts.toString()) }
            item { QuickStatCard("Total Volume", "${totalVolume / 1000}k kg") }
            item { QuickStatCard("Time Spent", "${totalDurationHours}h") }
        }

        // B. VOLUME GRAPH CARD
        VolumeGraphCard(history)

        // C. MUSCLE DISTRIBUTION CARD
        MuscleDistributionCard(history)
    }
}

@Composable
fun QuickStatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).widthIn(min = 100.dp)) {
            Text(label.uppercase(), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
fun VolumeGraphCard(history: List<FinishedWorkout>) {
    val stats = (0..6).map { dayOffset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
        val dayStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        val dayEnd = cal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
        
        val dayVolume = history.filter { it.date in dayStart..dayEnd }.sumOf { it.totalVolume }
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        sdf.format(cal.time) to dayVolume
    }.reversed()

    val maxVolume = stats.maxOf { it.second }.coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timeline, null, tint = HevyBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WEEKLY VOLUME", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { (day, volume) ->
                    val barHeight = (volume / maxVolume).toFloat()
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .fillMaxHeight(barHeight.coerceIn(0.05f, 1f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (volume > 0) HevyBlue else Color.DarkGray.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(day.uppercase(), fontSize = 9.sp, color = if (volume > 0) Color.White else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleDistributionCard(history: List<FinishedWorkout>) {
    // Extract muscle data from all finished workouts
    val muscleCounts = history.flatMap { it.exercises }
        .flatMap { it.primaryMuscles }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(5) // Top 5 muscles

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("MUSCLE DISTRIBUTION", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (muscleCounts.isEmpty()) {
                Text("No data yet", color = Color.DarkGray, fontSize = 12.sp)
            } else {
                muscleCounts.forEach { (muscle, count) ->
                    val totalSets = muscleCounts.sumOf { it.second }
                    val percentage = count.toFloat() / totalSets
                    
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(muscle.replaceFirstChar { it.uppercase() }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${(percentage * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = percentage,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = HevyBlue,
                            trackColor = Color.DarkGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(workout: FinishedWorkout, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(workout.date))
    val dayStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(workout.date))
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = workout.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                    Text(text = "$dayStr, $dateStr", color = Color.Gray, fontSize = 12.sp)
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Delete Workout", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                onDelete()
                                showMenu = false 
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoryStatItem("Volume", "${workout.totalVolume.toInt()} kg")
                HistoryStatItem("Sets", workout.totalSets.toString())
                val m = workout.durationSeconds / 60
                HistoryStatItem("Duration", "${m}m")
            }
        }
    }
}

@Composable
fun HistoryStatItem(label: String, value: String) {
    Column {
        Text(text = label.uppercase(), fontSize = 10.sp, color = Color.Gray, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold)
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
    }
}
