package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.learningkotlin.data.ExerciseLibrary
import com.example.learningkotlin.model.ExerciseDefinition

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenExercisePicker(
    onDismiss: () -> Unit,
    onExerciseSelected: (ExerciseDefinition) -> Unit,
    onShowInfo: (ExerciseDefinition) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<String?>(null) }
    var selectedEquipment by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) 
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background 
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { 
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurface) 
                        }
                        Text(
                            "Select Exercise", 
                            style = MaterialTheme.typography.titleLarge, 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or muscle...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Muscle Chips Row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChipItem(selected = selectedMuscle == null, text = "All Muscles") { selectedMuscle = null }
                        }
                        items(ExerciseLibrary.getAllMuscles()) { muscle ->
                            FilterChipItem(selected = selectedMuscle == muscle, text = muscle) { selectedMuscle = muscle }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Equipment Chips Row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChipItem(selected = selectedEquipment == null, text = "All Equipment") { selectedEquipment = null }
                        }
                        items(ExerciseLibrary.getAllEquipment()) { equipment ->
                            FilterChipItem(selected = selectedEquipment == equipment, text = equipment) { selectedEquipment = equipment }
                        }
                    }
                }

                val filtered = ExerciseLibrary.getDefinitions().filter {
                    (it.name.contains(searchQuery, ignoreCase = true) || it.primaryMuscles.any { m -> m.contains(searchQuery, ignoreCase = true) }) &&
                    (selectedMuscle == null || it.primaryMuscles.contains(selectedMuscle)) &&
                    (selectedEquipment == null || it.equipment == selectedEquipment)
                }

                val grouped = filtered.groupBy { it.primaryMuscles.firstOrNull()?.uppercase() ?: "OTHER" }
                    .toSortedMap()

                LazyColumn(modifier = Modifier.weight(1f)) {
                    grouped.forEach { (muscle, exercises) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = muscle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        items(exercises, key = { it.id }) { def ->
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = { Text(def.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                supportingContent = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val primaryColor = MaterialTheme.colorScheme.primary
                                        Text(def.primaryMuscles.joinToString(", ").uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (def.equipment != null) {
                                            Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                            Text(def.equipment.uppercase(), fontSize = 10.sp, color = primaryColor.copy(alpha = 0.7f))
                                        }
                                    }
                                },
                                leadingContent = {
                                    if (def.images.isNotEmpty()) {
                                        AsyncImage(
                                            model = "file:///android_asset/exercises/${def.images[0]}",
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)))
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = { onShowInfo(def) }) {
                                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    }
                                },
                                modifier = Modifier.clickable { onExerciseSelected(def) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(selected: Boolean, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(32.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text.replaceFirstChar { it.uppercase() },
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
