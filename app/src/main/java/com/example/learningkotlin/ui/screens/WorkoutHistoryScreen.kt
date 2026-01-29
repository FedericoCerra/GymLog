package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.data.ExerciseLibrary
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.theme.HevyBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    history: List<FinishedWorkout>,
    onWorkoutClick: (FinishedWorkout) -> Unit,
    onDeleteWorkout: (Int) -> Unit,
    bottomBarPadding: Dp = 0.dp
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
                item { HistoryDashboard(history) }
                
                item { ConsistencyHeatmapCard(history) }

                item {
                    Text(
                        text = "Recent Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(history, key = { it.id }) { workout ->
                    HistoryItem(
                        workout = workout, 
                        onClick = { onWorkoutClick(workout) },
                        onDelete = { onDeleteWorkout(workout.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(bottomBarPadding + 32.dp)) }
            }
        }
    }
}

@Composable
fun HistoryDashboard(history: List<FinishedWorkout>) {
    val totalWorkouts by remember { derivedStateOf { history.size } }
    val totalVolume by remember { derivedStateOf { history.sumOf { it.totalVolume } } }
    val totalDurationHours by remember { derivedStateOf { history.sumOf { it.durationSeconds } / 3600 } }

    val volumeDisplay by remember {
        derivedStateOf {
            when {
                totalVolume >= 1_000_000 -> "%.1fM kg".format(totalVolume / 1_000_000.0)
                totalVolume >= 1_000 -> "${(totalVolume / 1_000).toInt()}k kg"
                else -> "${totalVolume.toInt()} kg"
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item { QuickStatCard("Total Workouts", totalWorkouts.toString()) }
            item { QuickStatCard("Total Volume", volumeDisplay) }
            item { QuickStatCard("Time Spent", "${totalDurationHours}h") }
        }
        VolumeGraphCard(history)
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
    val stats by remember {
        derivedStateOf {
            (0..6).map { dayOffset ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                val dayEnd = cal.timeInMillis
                
                val dayVolume = history.filter { it.date in dayStart..dayEnd }.sumOf { it.totalVolume }
                // Use shorter day names and ensure they fit
                val dayName = SimpleDateFormat("EE", Locale.getDefault()).format(cal.time).replace(".", "").uppercase()
                dayName to dayVolume
            }.reversed()
        }
    }
    
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
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { (day, volume) ->
                    val barHeight = (volume / maxVolume).toFloat()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(barHeight.coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (volume > 0) HevyBlue else Color.DarkGray.copy(alpha = 0.3f))
                            )
                        }
                        
                        val volumeText = when {
                            volume >= 1000 -> "${(volume / 1000).toInt()}k"
                            volume > 0 -> volume.toInt().toString()
                            else -> ""
                        }
                        
                        Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.Center) {
                            if (volumeText.isNotEmpty()) {
                                Text(text = volumeText, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day, 
                            fontSize = 9.sp, 
                            color = if (volume > 0) Color.White else Color.Gray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConsistencyHeatmapCard(history: List<FinishedWorkout>) {
    val workoutDates by remember {
        derivedStateOf {
            history.map { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.date
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.toSet()
        }
    }

    // Dynamic locale-aware day labels (Single letter initials)
    val dayLabels = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        (0..6).map {
            SimpleDateFormat("EEEEE", Locale.getDefault()).format(cal.time).uppercase()
                .also { cal.add(Calendar.DAY_OF_YEAR, 1) }
        }
    }
    
    val weeksToShow = 6
    val weekLabelWidth = 24.dp
    val squareSize = 10.dp
    val gap = 4.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONSISTENCY", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row with day initials - aligned with squares
                Row(
                    modifier = Modifier.padding(start = weekLabelWidth + gap, bottom = 4.dp), 
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label, 
                            color = Color.Gray, 
                            fontSize = 8.sp, 
                            modifier = Modifier.width(squareSize), 
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Grid of workouts - Weeks are horizontal rows
                Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                    (weeksToShow - 1 downTo 0).forEach { weekOffset ->
                        Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "W${weeksToShow - weekOffset}", 
                                color = Color.DarkGray, 
                                fontSize = 8.sp, 
                                modifier = Modifier.width(weekLabelWidth),
                                fontWeight = FontWeight.Medium
                            )
                            
                            (0..6).forEach { dayIndex ->
                                val cal = Calendar.getInstance()
                                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                                val daysSinceMonday = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
                                cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
                                cal.add(Calendar.WEEK_OF_YEAR, -weekOffset)
                                cal.add(Calendar.DAY_OF_YEAR, dayIndex)
                                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                                
                                val dayTimestamp = cal.timeInMillis
                                val hasWorkout = workoutDates.contains(dayTimestamp)
                                val isToday = dayTimestamp == Calendar.getInstance().apply { 
                                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                }.timeInMillis

                                Box(
                                    modifier = Modifier
                                        .size(squareSize)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            when {
                                                hasWorkout -> HevyBlue
                                                isToday -> Color.DarkGray.copy(alpha = 0.5f)
                                                else -> Color.DarkGray.copy(alpha = 0.15f)
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Activity over the last $weeksToShow weeks", fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun MuscleDistributionCard(history: List<FinishedWorkout>) {
    val muscleCounts by remember {
        derivedStateOf {
            history.flatMap { workout ->
                workout.exercises.flatMap { exercise ->
                    if (exercise.primaryMuscles.isNotEmpty()) {
                        exercise.primaryMuscles
                    } else {
                        ExerciseLibrary.getDefinitions()
                            .find { it.name.equals(exercise.name, ignoreCase = true) }
                            ?.primaryMuscles ?: emptyList()
                    }
                }
            }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(6)
        }
    }

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
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = muscle.replaceFirstChar { it.uppercase() }, 
                                color = Color.White, 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(percentage * 100).toInt()}%", 
                                color = Color.Gray, 
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = HevyBlue,
                            trackColor = Color.DarkGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

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
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
