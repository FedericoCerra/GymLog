package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.viewmodel.HomeViewModel // Import your ViewModel
import com.example.learningkotlin.ui.components.homeScreenHelpers.HomeHeader
import com.example.learningkotlin.ui.components.homeScreenHelpers.WeeklySummaryCard
import com.example.learningkotlin.ui.components.homeScreenHelpers.WorkoutListItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,          // <--- 1. We now accept the Logic Toolbox
    onWorkoutClick: (Workout) -> Unit  // <--- 2. We still need this for Navigation
) {
    // A. Read data directly from ViewModel
    val workouts = viewModel.workouts

    // B. Local UI State (Popup visibility)
    var showDialog by remember { mutableStateOf(false) }
    var newWorkoutName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newWorkoutName = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Routine")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            item { HomeHeader() }

            // 2. Summary Card
            item { WeeklySummaryCard() }

            // 3. Section Title
            item {
                Text(
                    text = "My Routines",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // 4. Workout List
            items(workouts) { workout ->
                WorkoutListItem(
                    workout = workout,
                    onClick = { onWorkoutClick(workout) },

                    // C. Call ViewModel functions directly
                    onDelete = { viewModel.deleteWorkout(workout) },
                    onRename = { newName -> viewModel.renameWorkout(workout, newName) }
                )
            }

            // Spacer for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // 5. The Popup Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Routine") },
                text = {
                    OutlinedTextField(
                        value = newWorkoutName,
                        onValueChange = { newWorkoutName = it },
                        label = { Text("Routine Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newWorkoutName.isNotBlank()) {
                                // D. Call ViewModel to add
                                viewModel.addWorkout(newWorkoutName)
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Create")
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