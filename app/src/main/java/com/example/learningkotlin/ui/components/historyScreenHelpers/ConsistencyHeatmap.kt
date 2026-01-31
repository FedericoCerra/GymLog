package com.example.learningkotlin.ui.components.historyScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import java.util.*

@Composable
fun ConsistencyHeatmapContent(history: List<FinishedWorkout>) {
    val workoutDates = remember(history) {
        history.map {
            val cal = Calendar.getInstance().apply {
                timeInMillis = it.date
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.toSet()
    }

    val weeksToShow = 5
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    val primaryColor = MaterialTheme.colorScheme.primary

    val gridData = remember(history) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
        val currentMonday = today.clone() as Calendar
        currentMonday.add(Calendar.DAY_OF_YEAR, diffToMonday)

        (0 until weeksToShow).map { weekIndex ->
            val weekMonday = currentMonday.clone() as Calendar
            weekMonday.add(Calendar.WEEK_OF_YEAR, -(weeksToShow - 1 - weekIndex))
            
            (0 until 7).map { dayIndex ->
                val day = weekMonday.clone() as Calendar
                day.add(Calendar.DAY_OF_YEAR, dayIndex)
                day.timeInMillis
            }
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONSISTENCY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.ExtraBold)
            }
            Text("LATEST 5 WEEKS", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(70.dp))
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))

        gridData.forEachIndexed { index, week ->
            val weekOffset = weeksToShow - 1 - index
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when(weekOffset) {
                        0 -> "THIS WEEK"
                        1 -> "1W AGO"
                        else -> "${weekOffset}W AGO"
                    },
                    color = Color.DarkGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(70.dp)
                )

                week.forEach { timestamp ->
                    val hasWorkout = workoutDates.contains(timestamp)
                    val isToday = timestamp == Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (hasWorkout) primaryColor else Color(0xFF2C2C2E))
                            .let { 
                                if (isToday) it.border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                else it
                            }
                    )
                }
            }
        }
    }
}
