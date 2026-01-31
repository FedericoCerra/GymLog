package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.SetType
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.viewmodel.ExercisePersonalBests

@Composable
fun SetRow(
    index: Int,
    set: WorkoutSet,
    isWorkoutActive: Boolean,
    onDelete: () -> Unit,
    onCheck: (Boolean) -> Unit,
    onValueChange: () -> Unit = {},
    previousWeight: Double? = null,
    previousReps: Int? = null,
    personalBests: ExercisePersonalBests = ExercisePersonalBests()
) {
    // 1. Initialize State
    var weightText by remember(set.weight) {
        mutableStateOf(if (set.weight == 0.0) "" else set.weight.toString().removeSuffix(".0"))
    }
    var repsText by remember(set.reps) {
        mutableStateOf(if (set.reps == 0) "" else set.reps.toString())
    }

    var isChecked by remember { mutableStateOf(set.isDone) }
    var showMenu by remember { mutableStateOf(false) }
    var localSetType by remember(set.type) { mutableStateOf(set.type) }

    // Logic: PR check (Only if checked)
    val isWeightPR = remember(set.weight, personalBests.maxWeight, isChecked) {
        isChecked && set.weight > personalBests.maxWeight && personalBests.maxWeight > 0
    }
    val is1RMPR = remember(set.weight, set.reps, personalBests.max1RM, isChecked) {
        isChecked && set.calculate1RM() > personalBests.max1RM && personalBests.max1RM > 0
    }
    val isVolumePR = remember(set.weight, set.reps, personalBests.maxVolume, isChecked) {
        isChecked && set.calculateVolume() > personalBests.maxVolume && personalBests.maxVolume > 0
    }
    
    set.isWeightPR = isWeightPR
    set.is1RMPR = is1RMPR
    set.isVolumePR = isVolumePR

    val hasAnyPR = isWeightPR || is1RMPR || isVolumePR

    // Validation: Only allow checking if weight > 0 and reps > 0
    val isDataValid = (set.weight > 0.0 && set.reps > 0)
    val rowColor = if (isChecked) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Set Number and Type
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val typeText = when(localSetType) {
                SetType.NORMAL -> "$index"
                SetType.WARMUP -> "W"
                SetType.DROP -> "D"
                SetType.FAILURE -> "F"
            }
            val typeColor = when(localSetType) {
                SetType.NORMAL -> MaterialTheme.colorScheme.onSurface
                SetType.WARMUP -> Color(0xFFFFB300) // Orange
                SetType.DROP -> Color(0xFF9C27B0) // Purple
                SetType.FAILURE -> Color(0xFFE53935) // Red
            }

            Text(
                text = typeText,
                fontWeight = FontWeight.Black,
                color = typeColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMenu = true }
            )
            
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(surface = Color(0xFF1C1C1E)),
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))
            ) {
                DropdownMenu(
                    expanded = showMenu, 
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1C1C1E))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    SetType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (localSetType == type) MaterialTheme.colorScheme.primary else Color.White,
                                    fontWeight = if (localSetType == type) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                localSetType = type
                                set.type = type
                                onValueChange()
                                showMenu = false
                            }
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    DropdownMenuItem(
                        text = { Text("Delete Set", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }
        }

        // 2. Previous
        val previousText = if (previousWeight != null && previousReps != null) {
            "${previousWeight.toString().removeSuffix(".0")} x $previousReps"
        } else {
            "-"
        }
        Text(
            text = previousText,
            modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        // 3. Weight Input
        TableInput(
            value = weightText,
            onValueChange = {
                weightText = it
                set.weight = it.toDoubleOrNull() ?: 0.0
                onValueChange()
            },
            modifier = Modifier.weight(1.5f),
            imeAction = ImeAction.Next
        )

        // 4. Reps Input
        TableInput(
            value = repsText,
            onValueChange = {
                repsText = it
                set.reps = it.toIntOrNull() ?: 0
                onValueChange()
            },
            modifier = Modifier.weight(1.5f),
            imeAction = ImeAction.Done
        )

        // 5. Checkbox (Only visible if workout is ACTIVE)
        if (isWorkoutActive) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(
                        visible = hasAnyPR,
                        enter = fadeIn() + scaleIn()
                    ) {
                        Icon(
                            Icons.Default.Star, 
                            null, 
                            tint = Color(0xFFFFD700), 
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                    }
                    
                    Surface(
                        onClick = {
                            if (isDataValid) {
                                isChecked = !isChecked
                                set.isDone = isChecked
                                onCheck(isChecked)
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isChecked -> Color(0xFF4CAF50)
                            !isDataValid -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(28.dp),
                        enabled = isDataValid || isChecked // Allow unchecking even if data became invalid
                    ) {
                        if (isChecked) {
                            Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TableInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    imeAction: ImeAction,
    placeholder: String = "0"
) {
    val focusManager = LocalFocusManager.current
    
    // Detect if keyboard is hidden to clear focus
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Next) }
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
