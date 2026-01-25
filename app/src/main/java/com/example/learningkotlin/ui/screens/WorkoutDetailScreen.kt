package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.ui.components.BottomTimerBar
import com.example.learningkotlin.ui.components.ExerciseCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    onBackClick: () -> Unit,
    onAddExerciseClick: () -> Unit
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    // TIMER STATE
    var timerSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // TICKING ENGINE
    LaunchedEffect(key1 = timerSeconds, key2 = isTimerRunning) {
        if (isTimerRunning && timerSeconds > 0) {
            delay(1000L)
            timerSeconds -= 1
        } else if (timerSeconds == 0) {
            isTimerRunning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
        // DELETED: floatingActionButton = { ... }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. The Scrollable List
            key(refreshTrigger) {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // A. The Exercises
                    items(workout.exercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onStartTimer = { duration ->
                                timerSeconds = duration
                                isTimerRunning = true
                            }
                        )
                    }

                    // B. THE NEW "ADD EXERCISE" BUTTON (Inside the list)
                    item {
                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Subtle Blue
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

                    // Spacer so the bottom timer doesn't cover the last button
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }

            // 2. The Bottom Timer Popup (Floats on top)
            if (isTimerRunning) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .padding(innerPadding)
                ) {
                    BottomTimerBar(
                        secondsRemaining = timerSeconds,
                        onSkip = { isTimerRunning = false },
                        onAdd15 = { timerSeconds += 15 },
                        onSub15 = {
                            if (timerSeconds > 15) timerSeconds -= 15 else timerSeconds = 0
                        }
                    )
                }
            }

            // 3. The Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            val newId = (workout.exercises.maxOfOrNull { it.id } ?: 0) + 1
                            val newExercise = Exercise(
                                id = newId,
                                name = newExerciseName,
                                sets = mutableListOf(WorkoutSet(1, 0.0, 0, false)),
                                restTimer = 90
                            )
                            workout.exercises.add(newExercise)
                            refreshTrigger++
                            newExerciseName = ""
                            showDialog = false
                        }) { Text("Add") }
                    },
                    title = { Text("New Exercise") },
                    text = {
                        TextField(
                            value = newExerciseName,
                            onValueChange = { newExerciseName = it },
                            label = { Text("Name") },
                            singleLine = true
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}