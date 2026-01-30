package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.theme.HevyBlue
import com.example.learningkotlin.ui.components.common.StatCard
import com.example.learningkotlin.ui.components.common.StatItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    history: List<FinishedWorkout>,
    onWorkoutClick: (FinishedWorkout) -> Unit,
    onDeleteWorkout: (Int) -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workouts recorded yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header - Replaces TopAppBar to save space
                item {
                    Spacer(modifier = Modifier.height(padding.calculateTopPadding() + 8.dp))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                item { HistoryDashboard(history) }
                
                item { WorkoutStatsPagerCard(history) }

                item {
                    Text(
                        text = "Recent Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(history, key = { it.id }) { workout ->
                    HistoryItem(
                        workout = workout, 
                        onClick = { onWorkoutClick(workout) },
                        onDelete = { onDeleteWorkout(workout.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(bottomBarPadding + 32.dp)) }
            }
        }
    }
}

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
        // Stats row - Now takes full width with equal weighting
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard("Workouts", totalWorkouts.toString(), modifier = Modifier.weight(1f))
            StatCard("Volume", volumeDisplay, modifier = Modifier.weight(1f))
            StatCard("Time", "${totalDurationHours}h", modifier = Modifier.weight(1f))
        }
        VolumeGraphCard(history)
    }
}

@Composable
fun VolumeGraphCard(history: List<FinishedWorkout>) {
    val stats by remember {
        derivedStateOf {
            (0..6).map { dayOffset ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                val dayEnd = cal.timeInMillis
                
                val dayVolume = history.filter { it.date in dayStart..dayEnd }.sumOf { it.totalVolume }
                val dayName = SimpleDateFormat("EE", Locale.getDefault()).format(cal.time).replace(".", "").uppercase()
                dayName to dayVolume
            }.reversed()
        }
    }
    
    val maxVolume = stats.maxOf { it.second }.coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timeline, null, tint = HevyBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WEEKLY VOLUME", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp)) // Reduced from 24
            Row(
                modifier = Modifier.fillMaxWidth().height(110.dp), // Slightly shorter
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { (day, volume) ->
                    val barHeight = (volume / maxVolume).toFloat()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(barHeight.coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (volume > 0) HevyBlue else Color.DarkGray.copy(alpha = 0.3f))
                            )
                        }
                        
                        val volumeText = when {
                            volume >= 1000 -> "${(volume / 1000).toInt()}k"
                            volume > 0 -> volume.toInt().toString()
                            else -> ""
                        }
                        
                        Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.Center) {
                            if (volumeText.isNotEmpty()) {
                                Text(text = volumeText, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day, 
                            fontSize = 9.sp, 
                            color = if (volume > 0) Color.White else Color.Gray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutStatsPagerCard(history: List<FinishedWorkout>) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pager Indicator
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.DarkGray
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(6.dp)
                    )
                }
            }
        }
    }
}

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
        
        // Header for Days of the Week
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

        // Rows (Weeks)
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
                            .background(if (hasWorkout) HevyBlue else Color(0xFF2C2C2E))
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

@Composable
fun MuscleDistributionContent(history: List<FinishedWorkout>) {
    val weeksToShow = 5
    val filteredHistory = remember(history) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
            add(Calendar.DAY_OF_YEAR, diffToMonday)
            add(Calendar.WEEK_OF_YEAR, -(weeksToShow - 1))
        }
        val startTime = cal.timeInMillis
        history.filter { it.date >= startTime }
    }

    val distribution = remember(filteredHistory) { calculateMuscleDistribution(filteredHistory) }
    val labels = distribution.keys.toList()
    val values = distribution.values.map { it.toFloat() }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, null, tint = HevyBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("MUSCLE DISTRIBUTION", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.ExtraBold)
            }
            Text("LATEST 5 WEEKS", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (filteredHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().height(180.dp), contentAlignment = Alignment.Center) {
                Text("No data available for last 5 weeks", color = Color.DarkGray, fontSize = 12.sp)
            }
        } else {
            SpiderGraph(
                labels = labels,
                values = values,
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun SpiderGraph(
    labels: List<String>,
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.4f 
        val numAxes = labels.size
        val angleStep = (2 * PI / numAxes).toFloat()
        
        // Draw background web with subtle fill
        val levels = 4
        for (i in levels downTo 1) {
            val levelRadius = radius * (i.toFloat() / levels)
            val path = Path()
            for (j in 0 until numAxes) {
                val angle = j * angleStep - PI.toFloat() / 2
                val x = center.x + levelRadius * cos(angle)
                val y = center.y + levelRadius * sin(angle)
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.03f * i),
                style = Fill
            )
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.1f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI.toFloat() / 2
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            drawLine(Color.White.copy(alpha = 0.1f), center, Offset(x, y), strokeWidth = 1.dp.toPx())
        }

        if (values.any { it > 0 }) {
            val dataPoints = mutableListOf<Offset>()
            val dataPath = Path()
            for (i in 0 until numAxes) {
                val angle = i * angleStep - PI.toFloat() / 2
                val normalizedValue = values[i] / maxValue
                val valueRadius = radius * normalizedValue
                val point = Offset(center.x + valueRadius * cos(angle), center.y + valueRadius * sin(angle))
                dataPoints.add(point)
                if (i == 0) dataPath.moveTo(point.x, point.y) else dataPath.lineTo(point.x, point.y)
            }
            dataPath.close()
            
            val brush = Brush.radialGradient(
                colors = listOf(HevyBlue.copy(alpha = 0.4f), HevyBlue.copy(alpha = 0.1f)),
                center = center,
                radius = radius
            )
            drawPath(dataPath, brush, style = Fill)
            
            drawPath(
                path = dataPath,
                color = HevyBlue,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
                )
            )
            
            dataPoints.forEachIndexed { i, point ->
                if (values[i] > 0) {
                    drawCircle(Color.Black, radius = 4.dp.toPx(), center = point)
                    drawCircle(HevyBlue, radius = 2.5.dp.toPx(), center = point)
                }
            }
        }

        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI.toFloat() / 2
            val labelRadius = radius + 18.dp.toPx()
            val labelX = center.x + labelRadius * cos(angle)
            val labelY = center.y + labelRadius * sin(angle)
            
            val textLayoutResult = textMeasurer.measure(
                text = labels[i],
                style = TextStyle(
                    color = Color.LightGray, 
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )
            
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(labelX - textLayoutResult.size.width / 2, labelY - textLayoutResult.size.height / 2)
            )
        }
    }
}

private fun calculateMuscleDistribution(history: List<FinishedWorkout>): Map<String, Int> {
    val distribution = mutableMapOf(
        "CHEST" to 0,
        "BACK" to 0,
        "SHOULDERS" to 0,
        "ARMS" to 0,
        "LEGS" to 0,
        "ABS" to 0
    )

    history.forEach { workout ->
        workout.exercises.forEach { exercise ->
            val setsCount = exercise.sets.size
            exercise.primaryMuscles.forEach { muscle ->
                val group = when (muscle.lowercase()) {
                    "chest" -> "CHEST"
                    "back", "lats", "traps", "middle back", "lower back" -> "BACK"
                    "shoulders" -> "SHOULDERS"
                    "biceps", "triceps", "forearms" -> "ARMS"
                    "quadriceps", "hamstrings", "calves", "glutes" -> "LEGS"
                    "abdominals", "abs" -> "ABS"
                    else -> null
                }
                if (group != null) {
                    distribution[group] = (distribution[group] ?: 0) + setsCount
                }
            }
        }
    }
    return distribution
}

@Composable
fun HistoryItem(workout: FinishedWorkout, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(workout.date))
    val dayStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(workout.date))
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = workout.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                    Text(text = "$dayStr, $dateStr", color = Color.Gray, fontSize = 12.sp)
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Delete Workout", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Volume", "${workout.totalVolume.toInt()} kg")
                StatItem("Sets", workout.totalSets.toString())
                val m = workout.durationSeconds / 60
                StatItem("Duration", "${m}m")
            }
        }
    }
}
