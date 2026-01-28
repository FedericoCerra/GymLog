package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learningkotlin.model.Exercise

@Composable
fun ExerciseHeader(
    exercise: Exercise,
    onDeleteExercise: () -> Unit,
    onTimerChange: (Int) -> Unit,
    onInfoClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top, // Align items to the top to handle long titles
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. IMAGE THUMBNAIL
            if (exercise.imagePath != null) {
                AsyncImage(
                    model = "file:///android_asset/exercises/${exercise.imagePath}",
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1E)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 2. TEXT INFO (Title only here)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.clickable { onInfoClick() }
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
                            text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) },
                            onClick = { onDeleteExercise(); showMenu = false }
                        )
                    }
                }
            }
        }

        // 4. TIMER CHIP (Moved strictly under the title/image row)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            // Indent the timer slightly if there's an image so it aligns with the text
            if (exercise.imagePath != null) {
                Spacer(modifier = Modifier.width(60.dp)) // 48dp image + 12dp spacer
            }
            
            RestTimerChip(
                currentSeconds = exercise.restTimer,
                onTimeSelected = onTimerChange
            )
        }
    }
}