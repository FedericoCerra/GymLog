package com.example.learningkotlin.ui.components.homeScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learningkotlin.model.FinishedWorkout
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryCard(
    history: List<FinishedWorkout>,
    onClick: () -> Unit
) {
    // 1. Calculate workouts in the last 7 days
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val sevenDaysAgo = calendar.timeInMillis
    
    val workoutsThisWeek = history.count { it.date >= sevenDaysAgo }

    // 2. Logic for the mini-graph (last 7 days activity)
    val last7DaysActivity = (0..6).map { dayOffset ->
        val checkCal = Calendar.getInstance()
        checkCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
        val startOfDay = checkCal.apply { 
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) 
        }.timeInMillis
        val endOfDay = checkCal.apply { 
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) 
        }.timeInMillis
        
        history.any { it.date in startOfDay..endOfDay }
    }.reversed() // Show oldest to newest

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$workoutsThisWeek Workouts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Visual feedback: bars are taller if you worked out that day
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.height(30.dp)
            ) {
                last7DaysActivity.forEach { didWorkout ->
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(if (didWorkout) 1f else 0.3f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (didWorkout) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }
    }
}