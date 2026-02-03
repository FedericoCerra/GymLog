package com.federicocerra.gymlog.ui.components.workoutDetailScreenHelpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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
import com.federicocerra.gymlog.data.ExerciseLibrary

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
    onExerciseClick: () -> Unit,
    onNotesChange: (String) -> Unit,
    onClearSets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var localNotes by remember(notes) { mutableStateOf(notes) }
    var isExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f)

    // Glassmorphic background for the header area
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                1.5.dp, 
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.35f),
                    0.4f to Color.White.copy(alpha = 0.15f),
                    0.8f to Color.Transparent
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. LARGE IMAGE
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable { onExerciseClick() }
            ) {
                if (imagePath != null) {
                    AsyncImage(
                        model = "file:///android_asset/exercises/$imagePath",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. TEXT INFO
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exerciseName,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.clickable { onExerciseClick() }
                )
                
                // Expandable Description Trigger
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instructions",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).rotate(rotationState),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // 3. ACTIONS
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu, 
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1C1C1E)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Replace Exercise", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, "Replace", tint = Color.White) },
                        onClick = { onReplaceExercise(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear All Sets", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.PlaylistRemove, "Clear", tint = Color.White) },
                        onClick = { onClearSets(); showMenu = false }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    DropdownMenuItem(
                        text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) },
                        onClick = { onDeleteExercise(); showMenu = false }
                    )
                }
            }
        }

        // Expanded Description Content
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val instructions = remember(exerciseName) {
                ExerciseLibrary.getDefinitions().find { it.name == exerciseName }?.instructions ?: emptyList()
            }
            
            Column(modifier = Modifier.padding(top = 12.dp)) {
                if (instructions.isNotEmpty()) {
                    instructions.forEach { instruction ->
                        Text(
                            text = "• $instruction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                } else {
                    Text("No instructions available.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onInfoClick,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Full Guide", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // NOTES FIELD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = localNotes,
                onValueChange = { 
                    localNotes = it
                    onNotesChange(it)
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 13.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (localNotes.isEmpty()) {
                        Text("Add notes...", color = Color.Gray.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                    innerTextField()
                }
            )
        }

        // 4. TIMER CHIP
        Spacer(modifier = Modifier.height(12.dp))
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
