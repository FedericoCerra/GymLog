package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BottomTimerBar(
    secondsRemaining: Int,
    totalSeconds: Int,
    onSkip: () -> Unit,
    onAdd15: () -> Unit,
    onSub15: () -> Unit
) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "TimerProgress")

    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val threshold = -200f

    val isPastThreshold = offsetX.value < threshold

    val hintColor by animateColorAsState(
        targetValue = if (isPastThreshold) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.2f),
        label = "HintColor"
    )
    val hintScale by animateFloatAsState(
        targetValue = if (isPastThreshold) 1.2f else 0.8f,
        label = "HintScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp), // Sleeker height
        contentAlignment = Alignment.CenterEnd
    ) {
        // 1. MINIMALIST BACKGROUND HINT (An "X" icon revealed on swipe)
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = hintColor,
            modifier = Modifier
                .padding(end = 24.dp)
                .size(28.dp)
                .alpha((abs(offsetX.value) / 100f).coerceIn(0f, 1f))
                .scale(hintScale)
        )

        // 2. THE SWIPEABLE CARD
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount < 0 || offsetX.value < 0) {
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount)
                                }
                            }
                        },
                        onDragEnd = {
                            if (offsetX.value < threshold) {
                                onSkip()
                            } else {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    )
                }
                .shadow(12.dp, RoundedCornerShape(36.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(36.dp)),
            shape = RoundedCornerShape(36.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp) // Reduced internal padding
            ) {
                // LEFT ACTION
                TimerActionPill(
                    text = "-15s",
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = onSub15
                )

                // ABSOLUTE CENTER TIMER
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White.copy(alpha = 0.05f),
                        strokeWidth = 3.dp
                    )
                    CircularProgressIndicator(
                        progress = animatedProgress, // Fixed: Using Float for immediate reactivity
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        trackColor = Color.Transparent
                    )
                    Text(
                        text = timeText,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // RIGHT ACTION
                TimerActionPill(
                    text = "+15s",
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = onAdd15
                )
            }
        }
    }
}

@Composable
fun TimerActionPill(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
