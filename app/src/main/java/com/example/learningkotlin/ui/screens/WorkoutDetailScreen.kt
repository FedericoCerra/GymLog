package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.ui.components.ExerciseCard
import com.example.learningkotlin.ui.components.WorkoutHeaderStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    onBackClick: () -> Unit,
    onAddExerciseClick: () -> Unit
) {
    // 1. Calculate Stats
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
                        Text(workout.name, fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Finish")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // A. Header
            item {
                WorkoutHeaderStats(
                    duration = "5min",
                    volume = "${totalVolume.toInt()} kg",
                    sets = "$totalSets"
                )
            }

            // B. Exercise Cards
            items(workout.exercises) { exercise ->
                ExerciseCard(exercise = exercise)
            }

            // C. Spacer at bottom
            item { Spacer(modifier = Modifier.height(50.dp)) }
        }
    }
}