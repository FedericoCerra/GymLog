package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RestTimerChip(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    fun formatRestTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }

    Surface(
        onClick = { showDialog = true },
        shape = CircleShape,
        color = Color(0xFF1C1C1E),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = formatRestTime(currentSeconds), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Set Rest Timer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(32.dp))

                    var tempSeconds by remember { mutableIntStateOf(currentSeconds) }
                    
                    CircularTimePicker(
                        initialSeconds = tempSeconds,
                        onSecondsChange = { tempSeconds = it },
                        maxSeconds = 300 // 5 minutes for REST timer
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onTimeSelected(tempSeconds); showDialog = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Confirm ${formatRestTime(tempSeconds)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CircularTimePicker(
    initialSeconds: Int,
    onSecondsChange: (Int) -> Unit,
    maxSeconds: Int = 300
) {
    val focusManager = LocalFocusManager.current
    val isWorkoutDuration = maxSeconds > 600 // Logic to check if we're picking workout time or rest time
    
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            focusManager.clearFocus()
        }
    }
    
    // Determine labels based on usage
    val topLabel = if (isWorkoutDuration) (initialSeconds / 3600).toString() else (initialSeconds / 60).toString()
    val bottomLabel = if (isWorkoutDuration) ((initialSeconds % 3600) / 60).toString().padStart(2, '0') else (initialSeconds % 60).toString().padStart(2, '0')
    val subText = if (isWorkoutDuration) "HH:MM" else "MM:SS"

    var topText by remember(initialSeconds) { mutableStateOf(topLabel) }
    var bottomText by remember(initialSeconds) { mutableStateOf(bottomLabel) }
    
    val currentTotalSeconds = if (isWorkoutDuration) {
        (topText.toIntOrNull() ?: 0) * 3600 + (bottomText.toIntOrNull() ?: 0) * 60
    } else {
        (topText.toIntOrNull() ?: 0) * 60 + (bottomText.toIntOrNull() ?: 0)
    }
    
    var angle by remember(currentTotalSeconds) { 
        val bounded = currentTotalSeconds.coerceIn(0, maxSeconds)
        val initialAngle = (bounded.toFloat() / maxSeconds) * 360f - 90f
        mutableStateOf(initialAngle) 
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val touchX = change.position.x - (size.width / 2)
                        val touchY = change.position.y - (size.height / 2)
                        var newAngle = Math.toDegrees(atan2(touchY.toDouble(), touchX.toDouble())).toFloat()
                        
                        var normalizedAngle = (newAngle + 90f)
                        if (normalizedAngle < 0) normalizedAngle += 360f
                        
                        val newSeconds = ((normalizedAngle / 360f) * maxSeconds).toInt()
                        // Snapping: 5 mins for workout, 5 seconds for rest
                        val step = if (isWorkoutDuration) 300 else 5
                        val snappedSeconds = (newSeconds / step) * step
                        
                        if (snappedSeconds in 0..maxSeconds) {
                            angle = newAngle
                            if (isWorkoutDuration) {
                                topText = (snappedSeconds / 3600).toString()
                                bottomText = ((snappedSeconds % 3600) / 60).toString().padStart(2, '0')
                            } else {
                                topText = (snappedSeconds / 60).toString()
                                bottomText = (snappedSeconds % 60).toString().padStart(2, '0')
                            }
                            onSecondsChange(snappedSeconds)
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 20.dp.toPx()

            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius,
                center = center,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            val sweepAngle = ((angle + 90f + 360f) % 360f)
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = if (sweepAngle == 0f) 0.1f else sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            val handleRad = Math.toRadians(angle.toDouble())
            val handleX = center.x + radius * cos(handleRad).toFloat()
            val handleY = center.y + radius * sin(handleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(handleX, handleY)
            )
            drawCircle(
                color = primaryColor,
                radius = 8.dp.toPx(),
                center = Offset(handleX, handleY)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = topText,
                    onValueChange = {
                        if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                            topText = it
                            val multiplier = if (isWorkoutDuration) 3600 else 60
                            val newTotal = (it.toIntOrNull() ?: 0) * multiplier + 
                                           (bottomText.toIntOrNull() ?: 0) * (if (isWorkoutDuration) 60 else 1)
                            onSecondsChange(newTotal.coerceIn(0, maxSeconds))
                        }
                    },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Next) }),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.width(if (topText.length > 1) 60.dp else 45.dp)
                )
                
                Text(":", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
                
                BasicTextField(
                    value = bottomText,
                    onValueChange = {
                        if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                            bottomText = it
                            val multiplier = if (isWorkoutDuration) 3600 else 60
                            val newTotal = (topText.toIntOrNull() ?: 0) * multiplier + 
                                           (it.toIntOrNull() ?: 0) * (if (isWorkoutDuration) 60 else 1)
                            onSecondsChange(newTotal.coerceIn(0, maxSeconds))
                        }
                    },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.width(60.dp)
                )
            }
            Text(
                text = subText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 2.sp
            )
        }
    }
}
