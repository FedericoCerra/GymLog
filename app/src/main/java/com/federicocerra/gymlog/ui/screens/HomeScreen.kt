package com.federicocerra.gymlog.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.federicocerra.gymlog.model.Workout
import com.federicocerra.gymlog.viewmodel.HomeViewModel
import com.federicocerra.gymlog.ui.components.homeScreenHelpers.HomeHeader
import com.federicocerra.gymlog.ui.components.homeScreenHelpers.WeeklySummaryCard
import com.federicocerra.gymlog.ui.components.homeScreenHelpers.WorkoutListItem
import com.federicocerra.gymlog.ui.components.common.ModernDialog
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onWorkoutClick: (Workout) -> Unit,
    onSummaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val workouts = viewModel.workouts
    var showDialog by remember { mutableStateOf(false) }
    var newWorkoutName by remember { mutableStateOf("") }

    // Drag and drop state
    var draggedItemId by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    
    val density = LocalDensity.current
    val itemHeightPx = with(density) { (92.dp + 16.dp).toPx() } // Card height + spacing
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { 
                    HomeHeader(onProfileClick = onProfileClick) 
                }
                item { 
                    WeeklySummaryCard(
                        history = viewModel.history,
                        onClick = onSummaryClick
                    ) 
                }

                item {
                    Text(
                        text = "My Routines",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                itemsIndexed(workouts, key = { _, workout -> workout.id }) { index, workout ->
                    val isDragging = draggedItemId == workout.id
                    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "elevation")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .offset { 
                                if (isDragging) IntOffset(0, dragOffsetY.roundToInt()) 
                                else IntOffset.Zero 
                            }
                            .shadow(elevation, RoundedCornerShape(24.dp))
                            .pointerInput(workouts) { // Important: re-bind if workouts change
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { 
                                        draggedItemId = workout.id
                                        dragOffsetY = 0f
                                    },
                                    onDragEnd = { 
                                        draggedItemId = null
                                        dragOffsetY = 0f
                                        viewModel.onMoveEnd()
                                    },
                                    onDragCancel = { 
                                        draggedItemId = null
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                        
                                        val currentIndex = workouts.indexOfFirst { it.id == draggedItemId }
                                        if (currentIndex != -1) {
                                            if (dragOffsetY > itemHeightPx / 2 && currentIndex < workouts.size - 1) {
                                                viewModel.moveWorkout(currentIndex, currentIndex + 1)
                                                dragOffsetY -= itemHeightPx
                                            }
                                            else if (dragOffsetY < -itemHeightPx / 2 && currentIndex > 0) {
                                                viewModel.moveWorkout(currentIndex, currentIndex - 1)
                                                dragOffsetY += itemHeightPx
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        WorkoutListItem(
                            workout = workout,
                            onClick = { onWorkoutClick(workout) },
                            onDelete = { viewModel.deleteWorkout(workout) },
                            onRename = { newName -> viewModel.renameWorkout(workout, newName) },
                            isDragging = isDragging
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { 
                                newWorkoutName = ""
                                showDialog = true 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CREATE NEW ROUTINE",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(bottomBarPadding + 80.dp)) }
            }
        }

        if (showDialog) {
            ModernDialog(
                title = "New Routine",
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newWorkoutName.isNotBlank()) {
                                viewModel.addWorkout(newWorkoutName)
                                showDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            ) {
                OutlinedTextField(
                    value = newWorkoutName,
                    onValueChange = { newWorkoutName = it },
                    placeholder = { Text("Routine Name", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
