package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.model.SetType
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.TimeWheelPicker
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
                        }
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

@Composable
fun MuscleDistributionSection(muscleDistribution: List<Pair<String, Float>>, primaryColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Text(
            text = "MUSCLE DISTRIBUTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        muscleDistribution.forEach { (muscle, percentage) ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = muscle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "${(percentage * 100).toInt()}%", color = Color.Gray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .background(primaryColor)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isEditable: Boolean = false,
    onClick: (() -> Unit)? = null,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .then(if (onClick != null) Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(4.dp) else Modifier)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            if (isEditable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Edit, null, tint = primaryColor, modifier = Modifier.size(10.dp))
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecapExerciseItem(
    exercise: Exercise,
    primaryColor: Color,
    onUpdate: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    var localNotes by remember(exercise.notes) { mutableStateOf(exercise.notes) }

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val imagePath = exercise.imagePath
                if (imagePath != null) {
                    val coilModel = if (imagePath.startsWith("http")) imagePath else "file:///android_asset/exercises/$imagePath"
                    AsyncImage(
                        model = coilModel, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.FitnessCenter, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                BasicTextField(
                    value = localNotes,
                    onValueChange = { 
                        localNotes = it
                        exercise.notes = it
                        onUpdate()
                    },
                    textStyle = TextStyle(
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (localNotes.isEmpty()) {
                            Text("Add notes...", color = Color.DarkGray, fontSize = 13.sp, fontStyle = FontStyle.Italic)
                        }
                        innerTextField()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(text = "SET", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
            Text(text = "WEIGHT & REPS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        exercise.sets.forEachIndexed { index, set ->
            val isEven = index % 2 == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isEven) Color.Transparent else Color(0xFF1C1C1E))
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val typeText = when(set.type) {
                    SetType.NORMAL -> "${index + 1}"
                    SetType.WARMUP -> "W"
                    SetType.DROP -> "D"
                    SetType.FAILURE -> "F"
                }
                val typeColor = when(set.type) {
                    SetType.NORMAL -> Color.White
                    SetType.WARMUP -> Color(0xFFFFB300)
                    SetType.DROP -> Color(0xFF9C27B0)
                    SetType.FAILURE -> Color(0xFFE53935)
                }

                Text(
                    text = typeText, 
                    color = typeColor, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 16.sp, 
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Start
                )
                
                val weightStr = set.weight.toString().removeSuffix(".0")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$weightStr kg x ${set.reps}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    if (set.isWeightPR || set.is1RMPR || set.isVolumePR) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star, 
                                null, 
                                tint = Color(0xFFFFD700), 
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val prLabels = mutableListOf<String>()
                            if (set.isWeightPR) prLabels.add("WEIGHT")
                            if (set.is1RMPR) prLabels.add("1RM")
                            if (set.isVolumePR) prLabels.add("VOL")
                            
                            Text(
                                text = prLabels.joinToString(" & "),
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
