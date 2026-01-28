package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learningkotlin.data.ExerciseLibrary
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.ExerciseDefinition
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.ExerciseCard
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.WorkoutHeaderStats
import com.example.learningkotlin.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    viewModel: HomeViewModel,
    isAnyOtherWorkoutActive: Boolean,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onBackClick: () -> Unit,
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<String?>(null) }
    
    // Instructions Dialog State
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var exerciseForInstructions by remember { mutableStateOf<ExerciseDefinition?>(null) }

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

    val totalSets = remember(workout.exercises.size, refreshTrigger) { 
        workout.exercises.sumOf { it.sets.size } 
    }
    val completedSets = remember(refreshTrigger) { 
        workout.exercises.sumOf { it.sets.count { s -> s.isDone } } 
    }
    val totalVolume = remember(refreshTrigger) { 
        workout.exercises.sumOf { it.sets.sumOf { s -> s.weight * s.reps } } 
    }
    val completedVolume = remember(refreshTrigger) { 
        workout.exercises.sumOf { it.sets.filter { s -> s.isDone }.sumOf { s -> s.weight * s.reps } } 
    }
    val totalRestSeconds = remember(refreshTrigger) { 
        workout.exercises.sumOf { it.restTimer * it.sets.size } 
    }
    val estTimeSeconds = remember(refreshTrigger, totalSets) { 
        totalRestSeconds + (totalSets * 120) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (workout.isActive) {
                        TextButton(onClick = { 
                            onFinishWorkout() 
                            refreshTrigger++ 
                        }) {
                            Text("FINISH", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Button(
                            onClick = { 
                                onStartWorkout() 
                                refreshTrigger++ 
                            },
                            enabled = !isAnyOtherWorkoutActive
                        ) {
                            Text("START")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    WorkoutHeaderStats(
                        mainTimerLabel = if (workout.isActive) "Duration" else "Est. Time",
                        mainTimerValue = if (workout.isActive) formatDuration(workoutDurationSeconds) else formatDuration(estTimeSeconds.toLong()),
                        volumeLabel = if (workout.isActive) "Volume" else "Total Volume",
                        volumeValue = "${if (workout.isActive) completedVolume.toInt() else totalVolume.toInt()} kg",
                        setsLabel = if (workout.isActive) "Sets Done" else "Total Sets",
                        setsValue = if (workout.isActive) "$completedSets/$totalSets" else "$totalSets"
                    )
                }

                items(workout.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        isWorkoutActive = workout.isActive,
                        parentRefreshTrigger = refreshTrigger,
                        onUpdate = { refreshTrigger++ },
                        onRemove = {
                            workout.exercises.remove(exercise)
                            refreshTrigger++
                        },
                        onStartTimer = { duration ->
                            viewModel.startRestTimer(duration)
                        },
                        onInfoClick = {
                            val def = ExerciseLibrary.getDefinitions().find { it.name == exercise.name }
                            if (def != null) {
                                exerciseForInstructions = def
                                showInstructionsDialog = true
                            }
                        }
                    )
                }

                item {
                    Button(
                        onClick = { 
                            searchQuery = "" 
                            selectedMuscle = null
                            showDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Exercise")
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Select Exercise") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search exercises...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Muscle Filter Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedMuscle == null,
                                        onClick = { selectedMuscle = null },
                                        label = { Text("All") }
                                    )
                                }
                                items(ExerciseLibrary.getAllMuscles()) { muscle ->
                                    FilterChip(
                                        selected = selectedMuscle == muscle,
                                        onClick = { selectedMuscle = muscle },
                                        label = { Text(muscle.replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val filteredExercises = ExerciseLibrary.getDefinitions().filter {
                                (it.name.contains(searchQuery, ignoreCase = true) ||
                                it.primaryMuscles.any { muscle -> muscle.contains(searchQuery, ignoreCase = true) }) &&
                                (selectedMuscle == null || it.primaryMuscles.contains(selectedMuscle))
                            }

                            LazyColumn(modifier = Modifier.height(400.dp)) {
                                items(filteredExercises, key = { it.id }) { def ->
                                    ListItem(
                                        headlineContent = { Text(def.name, fontWeight = FontWeight.Bold) },
                                        supportingContent = { 
                                            Text(def.primaryMuscles.joinToString(", ").uppercase(), fontSize = 10.sp, color = Color.Gray)
                                        },
                                        leadingContent = {
                                            if (def.images.isNotEmpty()) {
                                                AsyncImage(
                                                    model = "file:///android_asset/exercises/${def.images[0]}",
                                                    contentDescription = null,
                                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(modifier = Modifier.size(50.dp).background(Color.DarkGray, RoundedCornerShape(4.dp)))
                                            }
                                        },
                                        trailingContent = {
                                            IconButton(onClick = {
                                                exerciseForInstructions = def
                                                showInstructionsDialog = true
                                            }) {
                                                Icon(Icons.Default.Info, "Instructions", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            val newId = (workout.exercises.maxOfOrNull { it.id } ?: 0) + 1
                                            val newExercise = Exercise(
                                                id = newId,
                                                name = def.name,
                                                sets = mutableListOf(WorkoutSet(1, 0.0, 0, false)),
                                                restTimer = 90
                                            )
                                            workout.exercises.add(newExercise)
                                            refreshTrigger++
                                            showDialog = false
                                        }
                                    )
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
            
            // Instructions Dialog
            if (showInstructionsDialog && exerciseForInstructions != null) {
                val exercise = exerciseForInstructions!!
                AlertDialog(
                    onDismissRequest = { showInstructionsDialog = false },
                    title = { Text(exercise.name, fontWeight = FontWeight.Bold) },
                    text = {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                            // Images Row
                            if (exercise.images.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        exercise.images.take(2).forEach { imgPath ->
                                            AsyncImage(
                                                model = "file:///android_asset/exercises/$imgPath",
                                                contentDescription = null,
                                                modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            
                            // Details
                            item {
                                Text("Equipment: ${exercise.equipment?.replaceFirstChar { it.uppercase() } ?: "None"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("Level: ${exercise.level?.replaceFirstChar { it.uppercase() } ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Instructions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            items(exercise.instructions) { instruction ->
                                Text(
                                    text = "• $instruction",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInstructionsDialog = false }) { Text("Got it") }
                    }
                )
            }
        }
    }
}