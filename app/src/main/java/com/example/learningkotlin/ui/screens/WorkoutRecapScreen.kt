package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun WorkoutRecapScreen(
    finishedWorkout: FinishedWorkout,
    onClose: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(finishedWorkout.date))
    
    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout Summary") },
                actions = {
                    TextButton(onClick = onClose) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = finishedWorkout.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RecapStat("Duration", formatDuration(finishedWorkout.durationSeconds))
                    RecapStat("Volume", "${finishedWorkout.totalVolume.toInt()} kg")
                    RecapStat("Sets", finishedWorkout.totalSets.toString())
                }
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(finishedWorkout.exercises) { exercise ->
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    exercise.sets.forEachIndexed { index, set ->
                        Text(
                            text = "Set ${index + 1}: ${set.weight.toInt()} kg x ${set.reps}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecapStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}