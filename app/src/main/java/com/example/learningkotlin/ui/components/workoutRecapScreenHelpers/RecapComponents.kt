package com.example.learningkotlin.ui.components.workoutRecapScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.SetType

@Composable
fun MuscleDistributionSection(muscleDistribution: List<Pair<String, Float>>, primaryColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Text(
            text = "MUSCLE DISTRIBUTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        muscleDistribution.forEach { (muscle, percentage) ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = muscle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "${(percentage * 100).toInt()}%", color = Color.Gray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .background(primaryColor)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isEditable: Boolean = false,
    onClick: (() -> Unit)? = null,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .then(if (onClick != null) Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(4.dp) else Modifier)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            if (isEditable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Edit, null, tint = primaryColor, modifier = Modifier.size(10.dp))
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecapExerciseItem(
    exercise: Exercise,
    primaryColor: Color,
    onUpdate: () -> Unit,
    onExerciseClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    var localNotes by remember(exercise.notes) { mutableStateOf(exercise.notes) }

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onExerciseClick() },
                contentAlignment = Alignment.Center
            ) {
                val imagePath = exercise.imagePath
                if (imagePath != null) {
                    val coilModel = if (imagePath.startsWith("http")) imagePath else "file:///android_asset/exercises/$imagePath"
                    AsyncImage(
                        model = coilModel, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.FitnessCenter, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name, 
                    color = primaryColor, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onExerciseClick() }
                )
                
                BasicTextField(
                    value = localNotes,
                    onValueChange = { 
                        localNotes = it
                        exercise.notes = it
                        onUpdate()
                    },
                    textStyle = TextStyle(
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (localNotes.isEmpty()) {
                            Text("Add notes...", color = Color.DarkGray, fontSize = 13.sp, fontStyle = FontStyle.Italic)
                        }
                        innerTextField()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(text = "SET", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
            Text(text = "WEIGHT & REPS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        exercise.sets.forEachIndexed { index, set ->
            val isEven = index % 2 == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isEven) Color.Transparent else Color(0xFF1C1C1E))
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val typeText = when(set.type) {
                    SetType.NORMAL -> "${index + 1}"
                    SetType.WARMUP -> "W"
                    SetType.DROP -> "D"
                    SetType.FAILURE -> "F"
                }
                val typeColor = when(set.type) {
                    SetType.NORMAL -> Color.White
                    SetType.WARMUP -> Color(0xFFFFB300)
                    SetType.DROP -> Color(0xFF9C27B0)
                    SetType.FAILURE -> Color(0xFFE53935)
                }

                Text(
                    text = typeText, 
                    color = typeColor, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 16.sp, 
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Start
                )
                
                val weightStr = set.weight.toString().removeSuffix(".0")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$weightStr kg x ${set.reps}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    if (set.isWeightPR || set.is1RMPR || set.isVolumePR) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (set.isWeightPR) Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            if (set.is1RMPR) Icon(Icons.Default.ElectricBolt, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp).padding(start = if (set.isWeightPR) 2.dp else 0.dp))
                            if (set.isVolumePR) Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF76FF03), modifier = Modifier.size(14.dp).padding(start = if (set.isWeightPR || set.is1RMPR) 2.dp else 0.dp))

                            Spacer(modifier = Modifier.width(4.dp))
                            val prLabels = mutableListOf<String>()
                            if (set.isWeightPR) prLabels.add("WEIGHT")
                            if (set.is1RMPR) prLabels.add("1RM")
                            if (set.isVolumePR) prLabels.add("VOL")
                            
                            Text(
                                text = prLabels.joinToString(" & "),
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
