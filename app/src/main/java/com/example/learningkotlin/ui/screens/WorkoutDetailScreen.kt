package com.example.learningkotlin.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.ExerciseDefinition
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.BottomTimerBar
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.ExerciseCard
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.WorkoutHeaderStats
import com.example.learningkotlin.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

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
) {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showSelectExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToReplace by remember { mutableStateOf<Exercise?>(null) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var exerciseForInstructions by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var showFinishConfirmationDialog by remember { mutableStateOf(false) }

    var workoutDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(key1 = workout.isActive) {
        while (workout.isActive) {
            val now = System.currentTimeMillis()
            val start = workout.startTime ?: now
            workoutDurationSeconds = (now - start) / 1000
            delay(1000L)
        }
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    // Force a new list instance on every refresh to ensure LazyColumn detects changes (adds/removes/updates)
    val workoutExercises = remember(refreshTrigger) { workout.exercises.toList() }

    val totalSets = remember(workoutExercises) { workoutExercises.sumOf { it.sets.size } }
    val completedSets = remember(workoutExercises) { workoutExercises.sumOf { it.sets.count { s -> s.isDone } } }
    val totalVolume = remember(workoutExercises) { workoutExercises.sumOf { it.sets.sumOf { s -> s.weight * s.reps } } }
    val completedVolume = remember(workoutExercises) { workoutExercises.sumOf { it.sets.filter { s -> s.isDone }.sumOf { s -> s.weight * s.reps } } }
    val totalRestSeconds = remember(workoutExercises) { workoutExercises.sumOf { it.restTimer * it.sets.size } }
    val estTimeSeconds = remember(totalRestSeconds, totalSets) { totalRestSeconds + (totalSets * 120) }

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
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    WorkoutHeaderStats(
                        mainTimerLabel = if (workout.isActive) "Duration" else "Est. Time",
                        mainTimerValue = if (workout.isActive) formatDuration(workoutDurationSeconds) else formatDuration(estTimeSeconds.toLong()),
                        volumeLabel = if (workout.isActive) "Volume" else "Total Volume",
                        volumeValue = "${if (workout.isActive) completedVolume.toInt() else totalVolume.toInt()} kg",
                        setsLabel = if (workout.isActive) "Sets Done" else "Total Sets",
                        setsValue = if (workout.isActive) "$completedSets/$totalSets" else "$totalSets"
                    )
                }

                items(workoutExercises, key = { it.id }) { exercise ->
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
                            workout.exercises.remove(exercise)
                            refreshTrigger++ 
                        },
                        onReplace = {
                            exerciseToReplace = exercise
                            showSelectExerciseDialog = true
                        },
                        onStartTimer = { duration -> viewModel.startRestTimer(duration) },
                        onInfoClick = {
                            val def = ExerciseLibrary.getDefinitions().find { it.name == exercise.name }
                            if (def != null) { exerciseForInstructions = def; showInstructionsDialog = true }
                        },
                        previousSets = previousSets,
                        personalBests = personalBests
                    )
                }

                item {
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
                item { Spacer(modifier = Modifier.height(100.dp)) }
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
                            // REPLACE LOGIC: Keep sets count, update info and values from history
                            exerciseToReplace?.let { ex ->
                                ex.name = def.name
                                ex.imagePath = if (def.images.isNotEmpty()) def.images[0] else null
                                
                                // Fetch history for the NEW exercise
                                val historicalSets = viewModel.getPreviousSetsForExercise(def.name)
                                
                                // Map historical values to existing sets
                                ex.sets.forEachIndexed { index, currentSet ->
                                    historicalSets.getOrNull(index)?.let { histSet ->
                                        currentSet.weight = histSet.weight
                                        currentSet.reps = histSet.reps
                                    } ?: run {
                                        // If no history for this set index, reset to 0
                                        currentSet.weight = 0.0
                                        currentSet.reps = 0
                                    }
                                }
                            }
                        } else {
                            // ADD LOGIC
                            val newId = (workout.exercises.maxOfOrNull { it.id } ?: 0) + 1
                            val historicalSets = viewModel.getPreviousSetsForExercise(def.name)
                            
                            // If history exists, pre-fill the first set. Otherwise 0.
                            val initialWeight = historicalSets.firstOrNull()?.weight ?: 0.0
                            val initialReps = historicalSets.firstOrNull()?.reps ?: 0

                            workout.exercises.add(
                                Exercise(
                                    id = newId, 
                                    name = def.name, 
                                    sets = mutableListOf(WorkoutSet(1, initialWeight, initialReps, false)),
                                    restTimer = 90,
                                    imagePath = if (def.images.isNotEmpty()) def.images[0] else null,
                                    primaryMuscles = def.primaryMuscles
                                )
                            )
                        }
                        refreshTrigger++
                        showSelectExerciseDialog = false
                        exerciseToReplace = null
                    },
                    onShowInfo = { def -> exerciseForInstructions = def; showInstructionsDialog = true }
                )
            }
            
            if (showInstructionsDialog && exerciseForInstructions != null) {
                val exercise = exerciseForInstructions!!
                AlertDialog(
                    onDismissRequest = { showInstructionsDialog = false },
                    title = { Text(exercise.name, fontWeight = FontWeight.Bold) },
                    text = {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                            if (exercise.images.isNotEmpty()) {
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        exercise.images.take(2).forEach { imgPath ->
                                            AsyncImage(
                                                model = "file:///android_asset/exercises/$imgPath",
                                                contentDescription = null,
                                                modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            item {
                                Text("Equipment: ${exercise.equipment?.uppercase() ?: "NONE"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Instructions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(exercise.instructions) { instruction ->
                                Text(text = "• $instruction", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showInstructionsDialog = false }) { Text("Got it") } }
                )
            }
        }
    }
}

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
            color = Color(0xFF0F0F0F) 
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        Text("Select Exercise", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or muscle...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.DarkGray
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
                                    .background(Color(0xFF1C1C1E))
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
                                headlineContent = { Text(def.name, fontWeight = FontWeight.Bold, color = Color.White) },
                                supportingContent = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(def.primaryMuscles.joinToString(", ").uppercase(), fontSize = 10.sp, color = Color.Gray)
                                        if (def.equipment != null) {
                                            Text(" • ", color = Color.DarkGray)
                                            Text(def.equipment.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
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
                                        Box(modifier = Modifier.size(56.dp).background(Color(0xFF1C1C1E), RoundedCornerShape(8.dp)))
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = { onShowInfo(def) }) {
                                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    }
                                },
                                modifier = Modifier.clickable { onExerciseSelected(def) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray.copy(alpha = 0.3f))
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
        color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF1C1C1E),
        modifier = Modifier.height(32.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text.replaceFirstChar { it.uppercase() },
                color = if (selected) Color.White else Color.Gray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
