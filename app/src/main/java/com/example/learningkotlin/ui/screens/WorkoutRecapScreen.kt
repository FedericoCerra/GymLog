package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.TimeWheelPicker
import com.example.learningkotlin.ui.components.workoutRecapScreenHelpers.MuscleDistributionSection
import com.example.learningkotlin.ui.components.workoutRecapScreenHelpers.RecapExerciseItem
import com.example.learningkotlin.ui.components.workoutRecapScreenHelpers.StatItem
import com.example.learningkotlin.ui.theme.HevyGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutRecapScreen(
    finishedWorkout: FinishedWorkout,
    onClose: () -> Unit,
    onExerciseClick: (String) -> Unit,
    onSave: ((FinishedWorkout) -> Unit)? = null
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    // Calculate Muscle Distribution
    val muscleDistribution = remember(finishedWorkout, refreshTrigger) {
        val counts = mutableMapOf<String, Int>()
        finishedWorkout.exercises.forEach { exercise ->
            val setCount = exercise.sets.size
            exercise.primaryMuscles.forEach { muscle ->
                counts[muscle] = (counts[muscle] ?: 0) + setCount
            }
        }
        val totalSets = counts.values.sum().toFloat()
        counts.map { (muscle, count) ->
            muscle to (if (totalSets > 0) count / totalSets else 0f)
        }.sortedByDescending { it.second }
    }

    // Formatting Helpers
    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%dh %02dm".format(h, m) else "%02dm %02ds".format(m, s)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        key(refreshTrigger) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Celebration Header
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp), 
                        tint = HevyGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Workout Complete!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = finishedWorkout.name, style = MaterialTheme.typography.titleLarge, color = primaryColor, fontWeight = FontWeight.Bold)

                    // DATE WITH EDIT ICON - Forced English Locale
                    val dateStr = SimpleDateFormat("EEEE, MMM d 'at' HH:mm", Locale.US).format(Date(finishedWorkout.date))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateStr, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Edit, null, tint = primaryColor, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // 2. Stats Grid
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem(
                                label = "Duration",
                                value = formatDuration(finishedWorkout.durationSeconds),
                                isEditable = true,
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(1f),
                                primaryColor = primaryColor
                            )
                            VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
                            StatItem(
                                label = "Volume",
                                value = "${finishedWorkout.totalVolume.toInt()} kg",
                                modifier = Modifier.weight(1f),
                                primaryColor = primaryColor
                            )
                            VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
                            StatItem(
                                label = "Sets",
                                value = finishedWorkout.totalSets.toString(),
                                modifier = Modifier.weight(1f),
                                primaryColor = primaryColor
                            )
                        }
                    }
                }

                // 3. Muscle Distribution Graph
                if (muscleDistribution.isNotEmpty()) {
                    item {
                        MuscleDistributionSection(muscleDistribution, primaryColor)
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 4. Exercise List
                items(finishedWorkout.exercises) { exercise ->
                    RecapExerciseItem(
                        exercise = exercise,
                        primaryColor = primaryColor,
                        onUpdate = { 
                            onSave?.invoke(finishedWorkout)
                        },
                        onExerciseClick = { onExerciseClick(exercise.name) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Recap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = finishedWorkout.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newDateMillis ->
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = finishedWorkout.date }
                        val newCalendar = Calendar.getInstance().apply { timeInMillis = newDateMillis }
                        newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                        newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                        finishedWorkout.date = newCalendar.timeInMillis
                        onSave?.invoke(finishedWorkout)
                        refreshTrigger++
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Edit Duration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(32.dp))
                    var tempSeconds by remember { mutableLongStateOf(finishedWorkout.durationSeconds) }
                    TimeWheelPicker(
                        initialTotalSeconds = tempSeconds.toInt(),
                        isHoursMode = true,
                        onTimeChange = { tempSeconds = it.toLong() }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            finishedWorkout.durationSeconds = tempSeconds
                            onSave?.invoke(finishedWorkout)
                            refreshTrigger++
                            showTimePicker = false
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Save Duration", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
