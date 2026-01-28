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
import com.example.learningkotlin.model.FinishedWorkout
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    history: List<FinishedWorkout>,
    onWorkoutClick: (FinishedWorkout) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Workout History") })
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No workouts recorded yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { workout ->
                    HistoryItem(workout, onClick = { onWorkoutClick(workout) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(workout: FinishedWorkout, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(workout.date))
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = workout.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = dateStr, color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Volume", fontSize = 12.sp, color = Color.Gray)
                    Text("${workout.totalVolume.toInt()} kg", fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Sets", fontSize = 12.sp, color = Color.Gray)
                    Text("${workout.totalSets}", fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Duration", fontSize = 12.sp, color = Color.Gray)
                    val m = workout.durationSeconds / 60
                    Text("${m}m", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}