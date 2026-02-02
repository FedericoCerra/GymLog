package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.learningkotlin.ui.components.historyScreenHelpers.HistorySummaryStats
import com.example.learningkotlin.ui.components.historyScreenHelpers.HistoryItem
import com.example.learningkotlin.ui.components.historyScreenHelpers.WeeklyStatsPager
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
        containerColor = MaterialTheme.colorScheme.background
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
                    color = MaterialTheme.colorScheme.onBackground
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
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(padding.calculateTopPadding() + 16.dp))
                    Text(
                        text = "Workout History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Overall Stats (Row of 3 separate cards)
                item { 
                    HistorySummaryStats(sortedHistory)
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Weekly Graphs (Separate glass card)
                item { 
                    WeeklyStatsPager(sortedHistory)
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Consistency & Muscles (Separate glass card)
                item { 
                    WorkoutStatsPagerCard(sortedHistory)
                    Spacer(modifier = Modifier.height(28.dp))
                }

                item {
                    Text(
                        text = "RECENT WORKOUTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                    )
                }

                itemsIndexed(sortedHistory, key = { _, workout -> workout.id }) { index, workout ->
                    Column {
                        HistoryItem(
                            workout = workout, 
                            allHistory = history,
                            onClick = { onWorkoutClick(workout) },
                            onDelete = { onDeleteWorkout(workout.id) }
                        )
                        
                        if (index < sortedHistory.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(bottomBarPadding + 40.dp)) }
            }
        }
    }
}
