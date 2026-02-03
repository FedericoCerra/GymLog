package com.federicocerra.gymlog.ui.components.homeScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.federicocerra.gymlog.model.FinishedWorkout
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryCard(
    history: List<FinishedWorkout>,
    onClick: () -> Unit
) {
    val stats by remember(history) {
        derivedStateOf {
            val cal = Calendar.getInstance()
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
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THIS WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$workoutsCount Workouts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                        .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "History >",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasWorkout -> primaryColor
                                        isToday -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        else -> Color.Transparent
                                    }
                                )
                                .let { 
                                    if (!hasWorkout && !isToday) it.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor = if (hasWorkout) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = initial,
                                color = textColor,
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
                                    .background(if (hasWorkout) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
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
