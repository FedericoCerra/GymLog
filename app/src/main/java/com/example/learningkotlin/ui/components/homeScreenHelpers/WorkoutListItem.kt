package com.example.learningkotlin.ui.components.homeScreenHelpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.ui.components.common.ModernDialog
import com.example.learningkotlin.service.WorkoutOverlayService

@Composable
fun WorkoutListItem(
    workout: Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renamedName by remember { mutableStateOf(workout.name) }
    
    val activeWorkoutId = WorkoutOverlayService.activeWorkoutId.intValue
    val isActive = activeWorkoutId == workout.id
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isActive) 2.dp else 1.dp, 
                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), 
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val exerciseCount = workout.exercises.size
                Text(
                    text = if (isActive) "Workout in progress..." else if (exerciseCount == 0) "No exercises" else "$exerciseCount exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium) },
                        onClick = {
                            showMenu = false
                            showRenameDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        ModernDialog(
            title = "Rename Routine",
            onDismissRequest = { showRenameDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (renamedName.isNotBlank()) {
                            onRename(renamedName)
                            showRenameDialog = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRenameDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            OutlinedTextField(
                value = renamedName,
                onValueChange = { renamedName = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}
