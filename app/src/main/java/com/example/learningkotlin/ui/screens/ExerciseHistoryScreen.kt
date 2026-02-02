package com.example.learningkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.data.ThemePreferences
import com.example.learningkotlin.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryScreen(
    exerciseName: String,
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val prs = viewModel.getPersonalBests(exerciseName)
    val history = viewModel.history.filter { workout ->
        workout.exercises.any { it.name == exerciseName }
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exerciseName, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. PERSONAL BESTS SECTION
            item {
                Text(
                    text = "PERSONAL BESTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRCard(
                        modifier = Modifier.weight(1f),
                        label = "Max Weight",
                        value = ThemePreferences.formatWeight(prs.maxWeight),
                        unit = ThemePreferences.weightUnit.value,
                        icon = Icons.Default.EmojiEvents,
                        color = primaryColor
                    )
                    PRCard(
                        modifier = Modifier.weight(1f),
                        label = "Max Volume",
                        value = ThemePreferences.formatWeight(prs.maxVolume),
                        unit = ThemePreferences.weightUnit.value,
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFF4CAF50)
                    )
                    PRCard(
                        modifier = Modifier.weight(1f),
                        label = "Est. 1RM",
                        value = ThemePreferences.formatWeight(prs.max1RM),
                        unit = ThemePreferences.weightUnit.value,
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // 2. HISTORY LIST
            item {
                Text(
                    text = "HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No history yet", color = Color.DarkGray)
                    }
                }
            }

            items(history) { workout ->
                val exercise = workout.exercises.find { it.name == exerciseName } ?: return@items
                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(workout.date))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = workout.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = dateStr, fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Show sets for this specific session
                        exercise.sets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Set ${index + 1}", fontSize = 13.sp, color = Color.Gray)
                                Row {
                                    Text(
                                        text = "${ThemePreferences.formatWeight(set.weight)} ${ThemePreferences.weightUnit.value}",
                                        fontSize = 13.sp,
                                        color = if (set.isWeightPR) primaryColor else Color.White,
                                        fontWeight = if (set.isWeightPR) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(text = " x ", fontSize = 13.sp, color = Color.Gray)
                                    Text(text = "${set.reps}", fontSize = 13.sp, color = Color.White)
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
fun PRCard(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier.height(100.dp),
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = "$unit $label", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}
