package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.theme.HevyBlue
import com.example.learningkotlin.ui.components.common.StatCard
import com.example.learningkotlin.ui.components.common.StatItem
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
        containerColor = Color.Black
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workouts recorded yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header - Replaces TopAppBar to save space
                item {
                    Spacer(modifier = Modifier.height(padding.calculateTopPadding() + 8.dp))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                item { HistoryDashboard(history) }
                
                item { ConsistencyHeatmapCard(history) }

                item {
                    Text(
                        text = "Recent Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
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
        // Stats row - Now takes full width with equal weighting
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard("Workouts", totalWorkouts.toString(), modifier = Modifier.weight(1f))
            StatCard("Volume", volumeDisplay, modifier = Modifier.weight(1f))
            StatCard("Time", "${totalDurationHours}h", modifier = Modifier.weight(1f))
        }
        VolumeGraphCard(history)
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
            Spacer(modifier = Modifier.height(16.dp)) // Reduced from 24
            Row(
                modifier = Modifier.fillMaxWidth().height(110.dp), // Slightly shorter
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
    val workoutDates = remember(history) {
        history.map {
            val cal = Calendar.getInstance().apply {
                timeInMillis = it.date
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.toSet()
    }

    val weeksToShow = 5
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    val gridData = remember(history) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
        val currentMonday = today.clone() as Calendar
        currentMonday.add(Calendar.DAY_OF_YEAR, diffToMonday)

        (0 until weeksToShow).map { weekIndex ->
            val weekMonday = currentMonday.clone() as Calendar
            weekMonday.add(Calendar.WEEK_OF_YEAR, -(weeksToShow - 1 - weekIndex))
            
            (0 until 7).map { dayIndex ->
                val day = weekMonday.clone() as Calendar
                day.add(Calendar.DAY_OF_YEAR, dayIndex)
                day.timeInMillis
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONSISTENCY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.ExtraBold)
                }
                Text("LATEST 5 WEEKS", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }
            

            // Header for Days of the Week
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(70.dp))
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))

            // Rows (Weeks)
            gridData.forEachIndexed { index, week ->
                val weekOffset = weeksToShow - 1 - index
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when(weekOffset) {
                            0 -> "THIS WEEK"
                            1 -> "1W AGO"
                            else -> "${weekOffset}W AGO"
                        },
                        color = Color.DarkGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp)
                    )

                    week.forEach { timestamp ->
                        val hasWorkout = workoutDates.contains(timestamp)
                        val isToday = timestamp == Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (hasWorkout) HevyBlue else Color(0xFF2C2C2E))
                                .let { 
                                    if (isToday) it.border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    else it
                                }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
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
                StatItem("Volume", "${workout.totalVolume.toInt()} kg")
                StatItem("Sets", workout.totalSets.toString())
                val m = workout.durationSeconds / 60
                StatItem("Duration", "${m}m")
            }
        }
    }
}
