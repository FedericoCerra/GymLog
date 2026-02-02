package com.example.learningkotlin.ui.components.historyScreenHelpers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.FinishedWorkout
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "MUSCLE DISTRIBUTION", 
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                "LATEST 5 WEEKS", 
                fontSize = 9.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (filteredHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().height(180.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No data available for last 5 weeks", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
                    fontSize = 12.sp
                )
            }
        } else {
            SpiderGraph(
                labels = labels,
                values = values,
                primaryColor = primaryColor,
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun SpiderGraph(
    labels: List<String>,
    values: List<Float>,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.4f 
        val numAxes = labels.size
        val angleStep = (2 * PI / numAxes).toFloat()
        
        // Background Webs
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
                color = onSurfaceColor.copy(alpha = 0.02f * i),
                style = Fill
            )
            drawPath(
                path = path,
                color = onSurfaceColor.copy(alpha = 0.08f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Axes lines
        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI.toFloat() / 2
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            drawLine(
                onSurfaceColor.copy(alpha = 0.08f), 
                center, 
                Offset(x, y), 
                strokeWidth = 1.dp.toPx()
            )
        }

        // Data Polygon
        if (values.any { it > 0 }) {
            val dataPoints = mutableListOf<Offset>()
            val dataPath = Path()
            
            for (i in 0 until numAxes) {
                val angle = i * angleStep - PI.toFloat() / 2
                
                // If value is 0, use a small 'base' radius (0.05) so it doesn't just pull to the center point.
                // This ensures the polygon always has some area and the fill is visible.
                val normalizedValue = if (values[i] > 0) {
                    (values[i] / maxValue).coerceAtLeast(0.15f)
                } else {
                    0.05f 
                }
                
                val valueRadius = radius * normalizedValue
                val point = Offset(center.x + valueRadius * cos(angle), center.y + valueRadius * sin(angle))
                dataPoints.add(point)
                if (i == 0) dataPath.moveTo(point.x, point.y) else dataPath.lineTo(point.x, point.y)
            }
            dataPath.close()
            
            val brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.5f), primaryColor.copy(alpha = 0.15f)),
                center = center,
                radius = radius
            )
            drawPath(dataPath, brush, style = Fill)
            
            drawPath(
                path = dataPath,
                color = primaryColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
                )
            )
            
            dataPoints.forEachIndexed { i, point ->
                if (values[i] > 0) {
                    drawCircle(onSurfaceColor, radius = 4.dp.toPx(), center = point)
                    drawCircle(primaryColor, radius = 2.5.dp.toPx(), center = point)
                }
            }
        }

        // Labels
        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI.toFloat() / 2
            val labelRadius = radius + 18.dp.toPx()
            val labelX = center.x + labelRadius * cos(angle)
            val labelY = center.y + labelRadius * sin(angle)
            
            val textLayoutResult = textMeasurer.measure(
                text = labels[i],
                style = TextStyle(
                    color = onSurfaceVariantColor,
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
