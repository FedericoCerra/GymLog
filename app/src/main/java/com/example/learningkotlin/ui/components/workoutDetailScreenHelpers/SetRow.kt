package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.data.ThemePreferences
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
    onPRDetected: (String, String) -> Unit = { _, _ -> },
    isBestWeightInWorkout: Boolean = false,
    isBest1RMInWorkout: Boolean = false,
    isBestVolumeInWorkout: Boolean = false,
    previousWeight: Double? = null,
    previousReps: Int? = null,
    personalBests: ExercisePersonalBests = ExercisePersonalBests()
) {
    val weightUnit = ThemePreferences.weightUnit.value
    val isLbs = weightUnit == "lbs"

    fun Double.toDisplay(): String = ThemePreferences.formatWeight(this)
    fun String.toKg(): Double {
        val d = this.toDoubleOrNull() ?: 0.0
        return if (isLbs) d / 2.20462 else d
    }

    var weightText by remember(set.weight, weightUnit) {
        mutableStateOf(if (set.weight == 0.0) "" else set.weight.toDisplay())
    }
    var repsText by remember {
        mutableStateOf(if (set.reps == 0) "" else set.reps.toString())
    }

    var isChecked by remember { mutableStateOf(set.isDone) }
    var showMenu by remember { mutableStateOf(false) }
    var localSetType by remember(set.type) { mutableStateOf(set.type) }

    // Logic for PR status relative to history
    val isWeightHistoricalPR = set.weight > 0 && (personalBests.maxWeight == 0.0 || set.weight > personalBests.maxWeight)
    val is1RMHistoricalPR = set.calculate1RM() > 0 && (personalBests.max1RM == 0.0 || set.calculate1RM() > personalBests.max1RM)
    val isVolumeHistoricalPR = set.calculateVolume() > 0 && (personalBests.maxVolume == 0.0 || set.calculateVolume() > personalBests.maxVolume)

    // A set gets a badge if it is a Historical PR AND it is the best currently done in this workout
    val hasWeightBadge = isChecked && isBestWeightInWorkout && isWeightHistoricalPR
    val has1RMBadge = isChecked && isBest1RMInWorkout && is1RMHistoricalPR
    val hasVolumeBadge = isChecked && isBestVolumeInWorkout && isVolumeHistoricalPR
    
    set.isWeightPR = hasWeightBadge
    set.is1RMPR = has1RMBadge
    set.isVolumePR = hasVolumeBadge

    val hasAnyBadge = hasWeightBadge || has1RMBadge || hasVolumeBadge

    val isDataValid = (set.weight > 0.0 && set.reps > 0)
    val rowColor = if (isChecked) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (hasAnyBadge) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "PR",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp).clickable { showMenu = true }
                )
            } else {
                val typeText = when(localSetType) {
                    SetType.NORMAL -> "$index"
                    SetType.WARMUP -> "W"
                    SetType.DROP -> "D"
                    SetType.FAILURE -> "F"
                }
                val typeColor = when(localSetType) {
                    SetType.NORMAL -> MaterialTheme.colorScheme.onSurface
                    SetType.WARMUP -> Color(0xFFFFB300)
                    SetType.DROP -> Color(0xFFAF52DE)
                    SetType.FAILURE -> MaterialTheme.colorScheme.error
                }

                Text(
                    text = typeText,
                    fontWeight = FontWeight.Black,
                    color = typeColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { showMenu = true }
                )
            }
            
            DropdownMenu(
                expanded = showMenu, 
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                SetType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (localSetType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                DropdownMenuItem(
                    text = { Text("Delete Set", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    onClick = { onDelete(); showMenu = false }
                )
            }
        }

        val previousText = if (previousWeight != null && previousReps != null) {
            "${previousWeight.toDisplay()} x $previousReps"
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

        TableInput(
            value = weightText,
            onValueChange = {
                weightText = it
                set.weight = it.toKg()
                onValueChange()
            },
            modifier = Modifier.weight(1.5f),
            imeAction = ImeAction.Next
        )

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

        if (isWorkoutActive) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Surface(
                    onClick = {
                        if (isDataValid) {
                            val newChecked = !isChecked
                            isChecked = newChecked
                            set.isDone = newChecked
                            
                            if (newChecked) {
                                // PR detection for notification overlay
                                when {
                                    is1RMHistoricalPR -> onPRDetected("New 1RM PR!", "1RM: ${set.calculate1RM().toDisplay()} $weightUnit")
                                    isWeightHistoricalPR -> onPRDetected("New Weight PR!", "${set.weight.toDisplay()} $weightUnit")
                                    isVolumeHistoricalPR -> onPRDetected("New Volume PR!", "Volume: ${set.calculateVolume().toDisplay()} $weightUnit")
                                }
                            }
                            
                            onCheck(newChecked)
                        }
                    },
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        isChecked -> Color(0xFF4CAF50)
                        !isDataValid -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(28.dp),
                    enabled = isDataValid || isChecked
                ) {
                    if (isChecked) {
                        Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TableInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    imeAction: ImeAction,
    placeholder: String = "0"
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier.padding(horizontal = 4.dp).height(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 16.sp, textAlign = TextAlign.Center)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }, onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Next) }),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
    }
}
