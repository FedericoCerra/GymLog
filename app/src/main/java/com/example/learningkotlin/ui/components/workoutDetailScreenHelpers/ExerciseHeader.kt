package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ExerciseHeader(
    exerciseName: String,
    restTimer: Int,
    notes: String,
    imagePath: String?,
    isWorkoutActive: Boolean,
    onDeleteExercise: () -> Unit,
    onReplaceExercise: () -> Unit,
    onTimerChange: (Int) -> Unit,
    onManualTimerStart: (Int) -> Unit,
    onInfoClick: () -> Unit,
    onNotesChange: (String) -> Unit,
    onClearSets: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var localNotes by remember(notes) { mutableStateOf(notes) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. IMAGE THUMBNAIL
            if (imagePath != null) {
                AsyncImage(
                    model = "file:///android_asset/exercises/$imagePath",
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1E)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 2. TEXT INFO (Title and Notes)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exerciseName,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.clickable { onInfoClick() }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // NOTES FIELD
                BasicTextField(
                    value = localNotes,
                    onValueChange = { 
                        localNotes = it
                        onNotesChange(it)
                    },
                    textStyle = TextStyle(
                        color = Color.Gray,
                        fontSize = 13.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (localNotes.isEmpty()) {
                            Text("Add notes...", color = Color.DarkGray, fontSize = 13.sp)
                        }
                        innerTextField()
                    }
                )
            }

            // 3. ACTIONS
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Info, 
                        "Info", 
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Replace Exercise") },
                            leadingIcon = { Icon(Icons.Default.Edit, "Replace") },
                            onClick = { onReplaceExercise(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Sets") },
                            leadingIcon = { Icon(Icons.Default.PlaylistRemove, "Clear") },
                            onClick = { onClearSets(); showMenu = false }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) },
                            onClick = { onDeleteExercise(); showMenu = false }
                        )
                    }
                }
            }
        }

        // 4. TIMER CHIP
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            RestTimerChip(
                currentSeconds = restTimer,
                isWorkoutActive = isWorkoutActive,
                onTimeSelected = onTimerChange,
                onManualStart = onManualTimerStart
            )
        }
    }
}
