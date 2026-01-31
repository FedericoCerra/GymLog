package com.example.learningkotlin.ui.components.historyScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.theme.HevyBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryDashboard(history: List<FinishedWorkout>) {
    val totalWorkouts by remember { derivedStateOf { history.size } }
    val totalVolume by remember { derivedStateOf { history.sumOf { it.totalVolume } } }
    val totalDurationHours by remember { derivedStateOf { history.sumOf { it.durationSeconds } / 3600 } }

    val volumeDisplay by remember {
        derivedStateOf {
            when {
                totalVolume >= 1_000_000 -> "%.1fM kg".format(totalVolume / 1_000_000.0)
                totalVolume >= 1_000 -> "${(totalVolume / 1_000).toInt()}k kg"
                else -> "${totalVolume.toInt()} kg"
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DashboardStatItem("Workouts", totalWorkouts.toString(), modifier = Modifier.weight(1f))
            DashboardStatItem("Volume", volumeDisplay, modifier = Modifier.weight(1f))
            DashboardStatItem("Time", "${totalDurationHours}h", modifier = Modifier.weight(1f))
        }
        WeeklyStatsPager(history)
    }
}

@Composable
fun DashboardStatItem(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(), 
                fontSize = 10.sp, 
                color = Color.Gray, 
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = value, 
                fontSize = 18.sp, 
                color = Color.White, 
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeklyStatsPager(history: List<FinishedWorkout>) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) { page ->
                when (page) {
                    0 -> WeeklyGraph(
                        history = history,
                        title = "WEEKLY VOLUME",
                        icon = Icons.Default.Timeline,
                        getValue = { it.totalVolume },
                        formatValue = { v -> if (v >= 1000) "${(v/1000).toInt()}k" else v.toInt().toString() }
                    )
                    1 -> WeeklyGraph(
                        history = history,
                        title = "WEEKLY TIME",
                        icon = Icons.Default.Schedule,
                        getValue = { it.durationSeconds.toDouble() / 60.0 },
                        formatValue = { v -> "${v.toInt()}m" }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.DarkGray
                    Box(modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(color).size(6.dp))
                }
            }
        }
    }
}

@Composable
fun WeeklyGraph(
    history: List<FinishedWorkout>,
    title: String,
    icon: ImageVector,
    getValue: (FinishedWorkout) -> Double,
    formatValue: (Double) -> String
) {
    val stats by remember(history) {
        derivedStateOf {
            (0..6).map { dayOffset ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
                val start = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                val end = cal.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                
                val dayValue = history.filter { it.date in start..end }.sumOf { getValue(it) }
                val dayName = SimpleDateFormat("EE", Locale.getDefault()).format(cal.time).replace(".", "").uppercase()
                dayName to dayValue
            }.reversed()
        }
    }
    
    val maxValue = stats.maxOf { it.second }.coerceAtLeast(1.0)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = HevyBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.forEach { (day, value) ->
                val barHeight = (value / maxValue).toFloat()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(barHeight.coerceIn(0.05f, 1f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (value > 0) HevyBlue else Color.DarkGray.copy(alpha = 0.2f))
                        )
                    }
                    Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.Center) {
                        if (value > 0) {
                            Text(text = formatValue(value), fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = day, fontSize = 9.sp, color = if (value > 0) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WorkoutStatsPagerCard(history: List<FinishedWorkout>) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(230.dp),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> ConsistencyHeatmapContent(history)
                    1 -> MuscleDistributionContent(history)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.DarkGray
                    Box(modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(color).size(6.dp))
                }
            }
        }
    }
}
