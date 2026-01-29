package com.example.learningkotlin.ui.components.homeScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.theme.HevyBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryCard(
    history: List<FinishedWorkout>,
    onClick: () -> Unit
) {
    // Use derivedStateOf to ensure stats update whenever the history list content changes
    val stats by remember(history) {
        derivedStateOf {
            val cal = Calendar.getInstance()
            // Get start of 7 days ago
            cal.add(Calendar.DAY_OF_YEAR, -6)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
            val rangeStart = cal.timeInMillis

            val workoutsThisWeek = history.count { it.date >= rangeStart }

            val dayData = (0..6).map { offset ->
                val checkCal = Calendar.getInstance()
                checkCal.add(Calendar.DAY_OF_YEAR, -(6 - offset))
                val start = checkCal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                val end = checkCal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                
                val hasWorkout = history.any { it.date in start..end }
                val dayInitial = SimpleDateFormat("EEEEE", Locale.getDefault()).format(checkCal.time).uppercase()
                val isToday = offset == 6
                
                Triple(dayInitial, hasWorkout, isToday)
            }
            
            Pair(workoutsThisWeek, dayData)
        }
    }

    val workoutsCount = stats.first
    val days = stats.second

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THIS WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$workoutsCount Workouts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                // Consistency "Pill"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HevyBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "History >",
                        color = HevyBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Day circles row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { (initial, hasWorkout, isToday) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasWorkout -> HevyBlue
                                        isToday -> Color.White.copy(alpha = 0.1f)
                                        else -> Color.Transparent
                                    }
                                )
                                .let { 
                                    if (!hasWorkout && !isToday) it.background(Color.DarkGray.copy(alpha = 0.2f))
                                    else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = if (hasWorkout) Color.White else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = if (hasWorkout || isToday) FontWeight.ExtraBold else FontWeight.Bold
                            )
                        }
                        
                        // Today indicator dot
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (hasWorkout) HevyBlue else Color.Gray)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(4.dp))
                        }
                    }
                }
            }
        }
    }
}
