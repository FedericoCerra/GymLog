package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.ui.components.ExerciseCard
import com.example.learningkotlin.ui.components.WorkoutHeaderStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    onBackClick: () -> Unit,
    onAddExerciseClick: () -> Unit // Kept for future use, but we use local dialog for now
) {
    // 1. REFRESH TRIGGER: Forces the whole list to redraw when we add a new exercise card
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // 2. DIALOG STATE: Controls if the popup is visible
    var showDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    // 3. LIVE STATS: These update automatically when refreshTrigger changes
    // We sum up the number of sets and the total volume (weight * reps)
    val totalSets = workout.exercises.sumOf { it.sets.size }
    val totalVolume = workout.exercises.sumOf { exercise ->
        exercise.sets.sumOf { set -> set.weight * set.reps }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Log Workout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        // Use typography for better dark mode support
                        Text(workout.name, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Finish")
                    }
                }
            )
        },
        // THE BLUE PLUS BUTTON (Floating Action Button)
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Open the popup
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { innerPadding ->

        // key(refreshTrigger) ensures the LazyColumn updates when we add an item
        key(refreshTrigger) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // A. Header Stats
                item {
                    WorkoutHeaderStats(
                        duration = "5min", // Still static for now
                        volume = "${totalVolume.toInt()} kg",
                        sets = "$totalSets"
                    )
                }

                // B. The Exercise Cards
                items(workout.exercises) { exercise ->
                    ExerciseCard(exercise = exercise)
                }

                // C. Extra space at bottom so FAB doesn't cover the last card
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // 4. THE POPUP DIALOG
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Exercise") },
                text = {
                    TextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = { Text("Exercise Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newExerciseName.isNotBlank()) {
                                // 1. Generate a new ID
                                val newId = (workout.exercises.maxOfOrNull { it.id } ?: 0) + 1

                                // 2. Create the exercise with ONE empty set by default
                                val newExercise = Exercise(
                                    id = newId,
                                    name = newExerciseName,
                                    sets = mutableListOf(
                                        WorkoutSet(
                                            id = 1,
                                            weight = 0.0,
                                            reps = 0,
                                            isDone = false
                                        )
                                    )
                                )

                                // 3. Add to the main workout object
                                workout.exercises.add(newExercise)

                                // 4. Refresh the UI and clean up
                                refreshTrigger++
                                newExerciseName = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}