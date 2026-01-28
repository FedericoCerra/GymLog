package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learningkotlin.data.ExerciseLibrary
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.BottomTimerBar
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.ExerciseCard
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.WorkoutHeaderStats
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    isAnyOtherWorkoutActive: Boolean, // <--- Added this parameter
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onBackClick: () -> Unit,
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // REST TIMER STATE
    var restTimerSeconds by remember { mutableIntStateOf(0) }
    var isRestTimerRunning by remember { mutableStateOf(false) }

    // WORKOUT DURATION STATE
    var workoutDurationSeconds by remember { mutableLongStateOf(0L) }

    // ENGINE FOR REST TIMER
    LaunchedEffect(key1 = restTimerSeconds, key2 = isRestTimerRunning) {
        if (isRestTimerRunning && restTimerSeconds > 0) {
            delay(1000L)
            restTimerSeconds -= 1
        } else if (restTimerSeconds == 0) {
            isRestTimerRunning = false
        }
    }

    // ENGINE FOR WORKOUT DURATION
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

    // CALCULATIONS FOR STATS
    val totalSets = workout.exercises.sumOf { it.sets.size }
    val completedSets = workout.exercises.sumOf { it.sets.count { s -> s.isDone } }
    
    val totalVolume = workout.exercises.sumOf { it.sets.sumOf { s -> s.weight * s.reps } }
    val completedVolume = workout.exercises.sumOf { it.sets.filter { s -> s.isDone }.sumOf { s -> s.weight * s.reps } }

    // Est. time: (total rest timers) + (number of sets * 2 mins)
    val totalRestSeconds = workout.exercises.sumOf { it.restTimer * it.sets.size }
    val estTimeSeconds = totalRestSeconds + (totalSets * 120)

    // Force recomposition
    key(refreshTrigger) {
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
                            // Only enable START if no other workout is active
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

                // 1. The Scrollable List
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // A. Workout Stats
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

                    // B. The Exercises
                    items(workout.exercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            isWorkoutActive = workout.isActive,
                            onUpdate = { refreshTrigger++ },
                            onRemove = {
                                workout.exercises.remove(exercise)
                                refreshTrigger++
                            },
                            onStartTimer = { duration ->
                                restTimerSeconds = duration
                                isRestTimerRunning = true
                            }
                        )
                    }

                    // C. ADD EXERCISE BUTTON
                    item {
                        Button(
                            onClick = { 
                                searchQuery = "" // Reset search
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

                if (isRestTimerRunning) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).padding(innerPadding)) {
                        BottomTimerBar(
                            secondsRemaining = restTimerSeconds,
                            onSkip = { isRestTimerRunning = false },
                            onAdd15 = { restTimerSeconds += 15 },
                            onSub15 = { if (restTimerSeconds > 15) restTimerSeconds -= 15 else restTimerSeconds = 0 }
                        )
                    }
                }

                // 3. SELECTION DIALOG
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
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val filteredExercises = ExerciseLibrary.commonExercises.filter {
                                    it.contains(searchQuery, ignoreCase = true)
                                }

                                LazyColumn(modifier = Modifier.height(300.dp)) {
                                    items(filteredExercises) { exerciseName ->
                                        ListItem(
                                            headlineContent = { Text(exerciseName) },
                                            modifier = Modifier.clickable {
                                                val newId = (workout.exercises.maxOfOrNull { it.id } ?: 0) + 1
                                                val newExercise = Exercise(
                                                    id = newId,
                                                    name = exerciseName,
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
            }
        }
    }
}