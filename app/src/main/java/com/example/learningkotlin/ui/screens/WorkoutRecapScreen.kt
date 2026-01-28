package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.TimeWheelPicker
import com.example.learningkotlin.ui.theme.HevyBlue
import com.example.learningkotlin.ui.theme.HevyGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutRecapScreen(
    finishedWorkout: FinishedWorkout,
    onClose: () -> Unit,
    onSave: ((FinishedWorkout) -> Unit)? = null
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        // Force recomposition when data changes
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
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(HevyGreen, HevyGreen.copy(alpha = 0.6f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(48.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Workout Complete!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = finishedWorkout.name, style = MaterialTheme.typography.titleLarge, color = HevyBlue, fontWeight = FontWeight.Bold)
                    
                    // CLICKABLE DATE
                    val dateStr = SimpleDateFormat("EEEE, MMM d 'at' HH:mm", Locale.getDefault()).format(Date(finishedWorkout.date))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = dateStr, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // 2. Editable Stats Grid
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EditableStatItem(
                                label = "Duration", 
                                value = formatDuration(finishedWorkout.durationSeconds),
                                onClick = { showTimePicker = true }
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.DarkGray)
                            RecapStatItem("Volume", "${finishedWorkout.totalVolume.toInt()} kg")
                            VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.DarkGray)
                            RecapStatItem("Sets", finishedWorkout.totalSets.toString())
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                items(finishedWorkout.exercises) { exercise ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = exercise.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HevyBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            exercise.sets.forEachIndexed { index, set ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(text = "${index + 1}", modifier = Modifier.weight(1f), color = Color.White, fontSize = 14.sp)
                                    Text(text = "${set.weight.toInt()} kg x ${set.reps}", modifier = Modifier.weight(3f), textAlign = androidx.compose.ui.text.style.TextAlign.End, color = Color.LightGray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HevyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Recap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    // --- MODIFICATION DIALOGS ---

    // 1. DATE PICKER (Now Preserves Original Time)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = finishedWorkout.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newDateMillis ->
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = finishedWorkout.date }
                        val newCalendar = Calendar.getInstance().apply { timeInMillis = newDateMillis }
                        
                        // Copy the specific time (HH:MM:SS) from original workout
                        newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                        newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                        newCalendar.set(Calendar.SECOND, oldCalendar.get(Calendar.SECOND))
                        
                        finishedWorkout.date = newCalendar.timeInMillis
                        onSave?.invoke(finishedWorkout)
                        refreshTrigger++ // Force immediate UI update
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 2. MODERN VERTICAL WHEEL DURATION PICKER
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
                    Text("Select hours and minutes spent training", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
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
                            refreshTrigger++ // Force immediate UI update
                            showTimePicker = false 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Duration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditableStatItem(label: String, value: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = HevyBlue, letterSpacing = 1.sp)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
fun RecapStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}