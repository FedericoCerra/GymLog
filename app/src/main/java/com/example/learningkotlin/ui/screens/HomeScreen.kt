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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.viewmodel.HomeViewModel
import com.example.learningkotlin.ui.components.homeScreenHelpers.HomeHeader
import com.example.learningkotlin.ui.components.homeScreenHelpers.WeeklySummaryCard
import com.example.learningkotlin.ui.components.homeScreenHelpers.WorkoutListItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onWorkoutClick: (Workout) -> Unit,
    onSummaryClick: () -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val workouts = viewModel.workouts
    var showDialog by remember { mutableStateOf(false) }
    var newWorkoutName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newWorkoutName = ""
                    showDialog = true
                },
                modifier = Modifier.padding(bottom = if (bottomBarPadding > 0.dp) bottomBarPadding - 24.dp else 16.dp),
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
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HomeHeader() }
            item { 
                WeeklySummaryCard(
                    history = viewModel.history,
                    onClick = onSummaryClick
                ) 
            }
            item {
                Text(
                    text = "My Routines",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(workouts) { workout ->
                WorkoutListItem(
                    workout = workout,
                    onClick = { onWorkoutClick(workout) },
                    onDelete = { viewModel.deleteWorkout(workout) },
                    onRename = { newName -> viewModel.renameWorkout(workout, newName) }
                )
            }

            item { Spacer(modifier = Modifier.height(bottomBarPadding + 80.dp)) }
        }

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