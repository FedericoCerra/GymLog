package com.federicocerra.gymlog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.federicocerra.gymlog.data.ExerciseLibrary
import com.federicocerra.gymlog.data.ThemePreferences
import com.federicocerra.gymlog.model.Exercise
import com.federicocerra.gymlog.model.ExerciseDefinition
import com.federicocerra.gymlog.model.Workout
import com.federicocerra.gymlog.model.WorkoutSet
import com.federicocerra.gymlog.ui.components.workoutDetailScreenHelpers.*
import com.federicocerra.gymlog.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class PREvent(
    val title: String,
    val description: String,
    val imagePath: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    viewModel: HomeViewModel,
    isAnyOtherWorkoutActive: Boolean,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onDiscardWorkout: () -> Unit,
    onBackClick: () -> Unit,
    onExerciseClick: (String) -> Unit,
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showSelectExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToReplace by remember { mutableStateOf<Exercise?>(null) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var exerciseForInstructions by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var showFinishConfirmationDialog by remember { mutableStateOf(false) }

    var currentPR by remember { mutableStateOf<PREvent?>(null) }
    var isPRCardExpanded by remember { mutableStateOf(false) }

    var workoutDurationSeconds by remember { mutableLongStateOf(0L) }
    val weightUnit = ThemePreferences.weightUnit.value

    LaunchedEffect(key1 = workout.isActive) {
        while (workout.isActive) {
            val now = System.currentTimeMillis()
            val start = workout.startTime ?: now
            workoutDurationSeconds = (now - start) / 1000
            delay(1000L)
        }
    }

    LaunchedEffect(currentPR) {
        val event = currentPR ?: return@LaunchedEffect
        isPRCardExpanded = false
        delay(600) // Slide down as circle
        isPRCardExpanded = true
        delay(3000)
        isPRCardExpanded = false
        delay(600) // Shrink back to circle
        if (currentPR == event) {
            currentPR = null
        }
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    fun convertWeight(kg: Double): String {
        val value = if (weightUnit == "lbs") kg * 2.20462 else kg
        return "%.1f %s".format(value, weightUnit).removeSuffix(".0 $weightUnit")
    }

    // Use a mutableStateList for easy reordering and immediate UI updates
    val exercises = remember(workout.id) { 
        mutableStateListOf<Exercise>().apply { addAll(workout.exercises) } 
    }

    val totalSets = remember(exercises.size, refreshTrigger) { exercises.sumOf { it.sets.size } }
    val completedSets = remember(exercises.size, refreshTrigger) { exercises.sumOf { it.sets.count { s -> s.isDone } } }
    
    val totalVolume = remember(exercises.size, refreshTrigger) { exercises.sumOf { it.sets.sumOf { s -> s.weight * s.reps } } }
    val completedVolume = remember(exercises.size, refreshTrigger) { exercises.sumOf { it.sets.filter { s -> s.isDone }.sumOf { s -> s.weight * s.reps } } }
    
    val totalRestSeconds = remember(exercises.size, refreshTrigger) { exercises.sumOf { it.restTimer * it.sets.size } }
    val estTimeSeconds = remember(totalRestSeconds, totalSets) { totalRestSeconds + (totalSets * 120) }

    // State for drag and drop
    var draggedItemId by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val itemHeightPx = with(density) { (72.dp + 12.dp).toPx() } // Compact card height + spacing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (workout.isActive) {
                        TextButton(onClick = { showFinishConfirmationDialog = true }) {
                            Text("FINISH", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Button(onClick = { onStartWorkout(); refreshTrigger++ }, enabled = !isAnyOtherWorkoutActive) {
                            Text("START")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (viewModel.isRestTimerRunning) {
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    BottomTimerBar(
                        secondsRemaining = viewModel.restTimerSeconds,
                        totalSeconds = viewModel.initialRestTimerSeconds,
                        onSkip = { viewModel.skipTimer() },
                        onAdd15 = { viewModel.add15Seconds() },
                        onSub15 = { viewModel.sub15Seconds() }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (draggedItemId != null) 12.dp else 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    WorkoutHeaderStats(
                        mainTimerLabel = if (workout.isActive) "Duration" else "Est. Time",
                        mainTimerValue = if (workout.isActive) formatDuration(workoutDurationSeconds) else formatDuration(estTimeSeconds.toLong()),
                        volumeLabel = if (workout.isActive) "Volume" else "Total Volume",
                        volumeValue = convertWeight(if (workout.isActive) completedVolume else totalVolume),
                        setsLabel = if (workout.isActive) "Sets Done" else "Total Sets",
                        setsValue = if (workout.isActive) "$completedSets/$totalSets" else "$totalSets"
                    )
                }

                itemsIndexed(exercises, key = { _, exercise -> exercise.id }) { _, exercise ->
                    val isDragging = draggedItemId == exercise.id
                    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .offset { 
                                if (isDragging) IntOffset(0, dragOffsetY.roundToInt()) 
                                else IntOffset.Zero 
                            }
                            .shadow(elevation, RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { 
                                        draggedItemId = exercise.id
                                        dragOffsetY = 0f
                                    },
                                    onDragEnd = { 
                                        draggedItemId = null
                                        dragOffsetY = 0f
                                        workout.exercises.clear()
                                        workout.exercises.addAll(exercises)
                                    },
                                    onDragCancel = { 
                                        draggedItemId = null
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                        
                                        val currentIndex = exercises.indexOfFirst { it.id == draggedItemId }
                                        if (currentIndex != -1) {
                                            if (dragOffsetY > itemHeightPx / 2 && currentIndex < exercises.size - 1) {
                                                exercises.add(currentIndex + 1, exercises.removeAt(currentIndex))
                                                dragOffsetY -= itemHeightPx
                                            }
                                            else if (dragOffsetY < -itemHeightPx / 2 && currentIndex > 0) {
                                                exercises.add(currentIndex - 1, exercises.removeAt(currentIndex))
                                                dragOffsetY += itemHeightPx
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        if (draggedItemId != null) {
                            CompactExerciseCard(exercise = exercise, isHighlighted = isDragging)
                        } else {
                            val previousSets = remember(exercise.name, viewModel.history) {
                                viewModel.getPreviousSetsForExercise(exercise.name)
                            }
                            val personalBests = remember(exercise.name, viewModel.history) {
                                viewModel.getPersonalBests(exercise.name)
                            }
                            
                            ExerciseCard(
                                exercise = exercise,
                                isWorkoutActive = workout.isActive,
                                onUpdate = { refreshTrigger++ },
                                onRemove = { 
                                    exercises.remove(exercise)
                                    workout.exercises.remove(exercise)
                                    refreshTrigger++ 
                                },
                                onReplace = {
                                    exerciseToReplace = exercise
                                    showSelectExerciseDialog = true
                                },
                                onStartTimer = { duration -> viewModel.onSetChecked(duration) },
                                onInfoClick = {
                                    val def = ExerciseLibrary.getDefinitions().find { it.name == exercise.name }
                                    if (def != null) { exerciseForInstructions = def; showInstructionsDialog = true }
                                },
                                onExerciseClick = { onExerciseClick(exercise.name) },
                                onPRDetected = { title, desc, img ->
                                    currentPR = PREvent(title, desc, img)
                                },
                                previousSets = previousSets,
                                personalBests = personalBests
                            )
                        }
                    }
                }

                item {
                    if (draggedItemId == null) {
                        Button(
                            onClick = { 
                                exerciseToReplace = null
                                showSelectExerciseDialog = true 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Exercise")
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            // Animated PR Notification Popup
            AnimatedVisibility(
                visible = currentPR != null,
                enter = slideInVertically(initialOffsetY = { -it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it * 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
                    .padding(top = innerPadding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp)
            ) {
                currentPR?.let { pr ->
                    Card(
                        modifier = Modifier
                            .height(80.dp)
                            .wrapContentWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(40.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular image
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                            ) {
                                if (pr.imagePath != null) {
                                    AsyncImage(
                                        model = "file:///android_asset/exercises/${pr.imagePath}",
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            
                            // Expanding Text Content
                            AnimatedVisibility(
                                visible = isPRCardExpanded,
                                enter = expandHorizontally() + fadeIn(),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.WorkspacePremium, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = pr.title, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Text(text = pr.description, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (showFinishConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showFinishConfirmationDialog = false },
                    title = { Text("Finish Workout") },
                    text = { Text("Do you want to save this workout to your history or discard it?") },
                    confirmButton = {
                        TextButton(onClick = { 
                            showFinishConfirmationDialog = false
                            onFinishWorkout()
                        }) {
                            Text("SAVE")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showFinishConfirmationDialog = false
                                onDiscardWorkout()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("DISCARD")
                        }
                    }
                )
            }

            if (showSelectExerciseDialog) {
                FullScreenExercisePicker(
                    onDismiss = { 
                        showSelectExerciseDialog = false
                        exerciseToReplace = null
                    },
                    onExerciseSelected = { def ->
                        if (exerciseToReplace != null) {
                            exerciseToReplace?.let { ex ->
                                ex.name = def.name
                                ex.imagePath = if (def.images.isNotEmpty()) def.images[0] else null
                                val historicalSets = viewModel.getPreviousSetsForExercise(def.name)
                                ex.sets.forEachIndexed { index, currentSet ->
                                    historicalSets.getOrNull(index)?.let { histSet ->
                                        currentSet.weight = histSet.weight
                                        currentSet.reps = histSet.reps
                                    } ?: run {
                                        currentSet.weight = 0.0
                                        currentSet.reps = 0
                                    }
                                }
                            }
                        } else {
                            val newId = (exercises.maxOfOrNull { it.id } ?: 0) + 1
                            val historicalSets = viewModel.getPreviousSetsForExercise(def.name)
                            val initialWeight = historicalSets.firstOrNull()?.weight ?: 0.0
                            val initialReps = historicalSets.firstOrNull()?.reps ?: 0

                            val newExercise = Exercise(
                                id = newId, 
                                name = def.name, 
                                sets = mutableListOf(WorkoutSet(1, initialWeight, initialReps, false)),
                                restTimer = 90,
                                imagePath = if (def.images.isNotEmpty()) def.images[0] else null,
                                primaryMuscles = def.primaryMuscles
                            )
                            exercises.add(newExercise)
                            workout.exercises.add(newExercise)
                        }
                        refreshTrigger++
                        showSelectExerciseDialog = false
                        exerciseToReplace = null
                    },
                    onShowInfo = { def -> exerciseForInstructions = def; showInstructionsDialog = true }
                )
            }
            
            if (showInstructionsDialog && exerciseForInstructions != null) {
                ExerciseInstructionsDialog(
                    exercise = exerciseForInstructions!!,
                    onDismiss = { showInstructionsDialog = false }
                )
            }
        }
    }
}

@Composable
fun CompactExerciseCard(exercise: Exercise, isHighlighted: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .then(
                if (isHighlighted) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                if (exercise.imagePath != null) {
                    AsyncImage(
                        model = "file:///android_asset/exercises/${exercise.imagePath}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = exercise.name,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.DragHandle, 
                contentDescription = null, 
                tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
