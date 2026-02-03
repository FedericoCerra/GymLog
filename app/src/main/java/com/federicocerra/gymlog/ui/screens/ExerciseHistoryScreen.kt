package com.federicocerra.gymlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.federicocerra.gymlog.data.ExerciseLibrary
import com.federicocerra.gymlog.data.ThemePreferences
import com.federicocerra.gymlog.model.FinishedWorkout
import com.federicocerra.gymlog.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryScreen(
    exerciseName: String,
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onWorkoutClick: (FinishedWorkout) -> Unit = {}
) {
    val prs = viewModel.getPersonalBests(exerciseName)
    val history = viewModel.history.filter { workout ->
        workout.exercises.any { it.name == exerciseName }
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val exerciseDef = remember(exerciseName) {
        ExerciseLibrary.getDefinitions().find { it.name == exerciseName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = exerciseName, 
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER IMAGE WITH GRADIENT OVERLAY
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (exerciseDef?.images?.isNotEmpty() == true) {
                        AsyncImage(
                            model = "file:///android_asset/exercises/${exerciseDef.images.first()}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Glassmorphic Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, null, tint = primaryColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exerciseDef?.equipment?.uppercase() ?: "EQUIPMENT",
                                color = primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = exerciseDef?.primaryMuscles?.joinToString(", ")?.uppercase() ?: "MUSCLES",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // PERSONAL BESTS SECTION (Glassmorphic Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PRCardGlass(
                        modifier = Modifier.weight(1f),
                        label = "Max Weight",
                        value = ThemePreferences.formatWeight(prs.maxWeight),
                        unit = ThemePreferences.weightUnit.value,
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFFFD700)
                    )
                    PRCardGlass(
                        modifier = Modifier.weight(1f),
                        label = "Volume",
                        value = ThemePreferences.formatWeight(prs.maxVolume),
                        unit = ThemePreferences.weightUnit.value,
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF76FF03)
                    )
                    PRCardGlass(
                        modifier = Modifier.weight(1f),
                        label = "Est. 1RM",
                        value = ThemePreferences.formatWeight(prs.max1RM),
                        unit = ThemePreferences.weightUnit.value,
                        icon = Icons.Default.ElectricBolt,
                        color = Color(0xFF00E5FF)
                    )
                }
            }

            // HISTORY LIST
            item {
                Text(
                    text = "LATEST SESSIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No training history yet", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            items(history.reversed()) { workout ->
                val exercise = workout.exercises.find { it.name == exerciseName } ?: return@items
                val date = Date(workout.date)
                val dayMonth = SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWorkoutClick(workout) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, 
                        Brush.verticalGradient(
                            0.0f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            0.2f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            0.5f to Color.Transparent
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = workout.name, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                                Text(text = year, fontSize = 11.sp, color = Color.Gray)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dayMonth.uppercase(), 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = primaryColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Set Details
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            exercise.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.Black,
                                        color = Color.Gray,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // WEIGHT x REPS
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${ThemePreferences.formatWeight(set.weight)} x ${set.reps}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = " ${ThemePreferences.weightUnit.value}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            
                                            // PR ICONS
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (set.isWeightPR) {
                                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                if (set.is1RMPR) {
                                                    Icon(Icons.Default.ElectricBolt, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                if (set.isVolumePR) {
                                                    Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF76FF03), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun PRCardGlass(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                    )
                )
            )
            .border(
                1.5.dp, 
                Brush.verticalGradient(
                    0.0f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    0.25f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    0.6f to Color.Transparent
                ),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " $unit", 
                        fontSize = 11.sp, 
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = label.uppercase(), 
                    fontSize = 9.sp, 
                    color = Color.Gray, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
