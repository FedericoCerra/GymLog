package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.components.historyScreenHelpers.HistoryDashboard
import com.example.learningkotlin.ui.components.historyScreenHelpers.HistoryItem
import com.example.learningkotlin.ui.components.historyScreenHelpers.WorkoutStatsPagerCard

@Composable
fun WorkoutHistoryScreen(
    history: List<FinishedWorkout>,
    onWorkoutClick: (FinishedWorkout) -> Unit,
    onDeleteWorkout: (Int) -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val sortedHistory = remember(history) {
        history.sortedByDescending { it.date }
    }

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        if (sortedHistory.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No History Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Finish a workout to get started and track your progress!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(padding.calculateTopPadding() + 8.dp))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                item { HistoryDashboard(sortedHistory) }
                
                item { WorkoutStatsPagerCard(sortedHistory) }

                item {
                    Text(
                        text = "Recent Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(sortedHistory, key = { it.id }) { workout ->
                    HistoryItem(
                        workout = workout, 
                        allHistory = history,
                        onClick = { onWorkoutClick(workout) },
                        onDelete = { onDeleteWorkout(workout.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(bottomBarPadding + 32.dp)) }
            }
        }
    }
}
