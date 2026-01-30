package com.example.learningkotlin.ui.components.homeScreenHelpers

import androidx.compose.foundation.border
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
import com.example.learningkotlin.ui.theme.HevyBlue

@Composable
fun WorkoutListItem(
    workout: Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // State for the Rename Dialog
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    val containerColor = Color(0xFF1C1C1E)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (workout.isActive) Modifier.border(2.dp, HevyBlue, RoundedCornerShape(12.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (workout.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = HevyBlue,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${workout.exercises.size} Exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // MENU BUTTON
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Gray
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // 1. RENAME OPTION
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, "Edit") },
                        onClick = {
                            renameText = workout.name // Load current name
                            showRenameDialog = true   // Show popup
                            showMenu = false
                        }
                    )

                    // 2. DELETE OPTION
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }

    // THE RENAME DIALOG
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Routine") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameText.isNotBlank()) {
                        onRename(renameText) // <--- Send new name up
                        showRenameDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}
