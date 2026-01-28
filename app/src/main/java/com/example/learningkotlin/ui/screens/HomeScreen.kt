package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = Color.Transparent
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
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 1. YOUR ROUTINES LIST
            items(workouts) { workout ->
                WorkoutListItem(
                    workout = workout,
                    onClick = { onWorkoutClick(workout) },
                    onDelete = { viewModel.deleteWorkout(workout) },
                    onRename = { newName -> viewModel.renameWorkout(workout, newName) }
                )
            }

            // 2. MODERN "ADD" CARD AT THE BOTTOM
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { 
                            newWorkoutName = ""
                            showDialog = true 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREATE NEW ROUTINE",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(bottomBarPadding + 80.dp)) }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Routine", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newWorkoutName,
                        onValueChange = { newWorkoutName = it },
                        label = { Text("Routine Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newWorkoutName.isNotBlank()) {
                                viewModel.addWorkout(newWorkoutName)
                                showDialog = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
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