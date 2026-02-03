package com.federicocerra.gymlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.federicocerra.gymlog.data.ThemePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentPrimary = ThemePreferences.primaryColor.value
    val currentWeightUnit = ThemePreferences.weightUnit.value
    val isAutoRestTimerEnabled = ThemePreferences.autoRestTimer.value
    val isTimerSoundEnabled = ThemePreferences.timerSound.value
    val showOverlayBubble = ThemePreferences.showOverlayBubble.value
    val showWorkoutNotification = ThemePreferences.showWorkoutNotification.value

    val availableColors = listOf(
        Color(0xFF2196F3), // Hevy Blue
        Color(0xFF34C759), // Green
        Color(0xFFFF3B30), // Red
        Color(0xFFAF52DE), // Purple
        Color(0xFFFF9500), // Orange
        Color(0xFF5856D6), // Indigo
        Color(0xFFFF2D55), // Pink
        Color(0xFF007AFF), // Apple Blue
        Color(0xFF5AC8FA), // Light Blue
        Color(0xFFFFCC00), // Yellow
        Color(0xFF8E8E93), // Grey
        Color(0xFF1C1C1E)  // Dark
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.ExtraBold) },
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. THEME SELECTION
            item {
                GlassSettingContainer(title = "APPEARANCE", subtitle = "Personalize your experience") {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 56.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(availableColors) { color ->
                            val isSelected = color == currentPrimary
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f))))
                                    .clickable { ThemePreferences.saveColor(context, color) }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // 2. UNITS & PREFERENCES
            item {
                GlassSettingContainer(title = "UNITS", subtitle = "Weights and measurements") {
                    ToggleSettingItem(
                        icon = Icons.Default.MonitorWeight,
                        label = "Weight Unit",
                        value = if (currentWeightUnit == "kg") "Kilograms (kg)" else "Pounds (lbs)",
                        onToggle = { 
                            val nextUnit = if (currentWeightUnit == "kg") "lbs" else "kg"
                            ThemePreferences.saveWeightUnit(context, nextUnit)
                        }
                    )
                }
            }

            // 3. WORKOUT PREFERENCES
            item {
                GlassSettingContainer(title = "WORKOUT", subtitle = "Automation and timing") {
                    SwitchSettingItem(
                        icon = Icons.Default.Timer,
                        label = "Auto Rest Timer",
                        description = "Start timer after each set",
                        checked = isAutoRestTimerEnabled,
                        onCheckedChange = { ThemePreferences.saveAutoRestTimer(context, it) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                    SwitchSettingItem(
                        icon = Icons.Default.NotificationsActive,
                        label = "Timer Sound",
                        description = "Play alert when rest ends",
                        checked = isTimerSoundEnabled,
                        onCheckedChange = { ThemePreferences.saveTimerSound(context, it) }
                    )
                }
            }

            // 4. NOTIFICATIONS & OVERLAY
            item {
                GlassSettingContainer(title = "NOTIFICATIONS", subtitle = "Visibility and alerts") {
                    SwitchSettingItem(
                        icon = Icons.Default.BubbleChart,
                        label = "Overlay Bubble",
                        description = "Show floating timer when outside app",
                        checked = showOverlayBubble,
                        onCheckedChange = { ThemePreferences.saveShowOverlayBubble(context, it) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                    SwitchSettingItem(
                        icon = Icons.Default.NotificationImportant,
                        label = "Workout Notification",
                        description = "Show active workout in tray",
                        checked = showWorkoutNotification,
                        onCheckedChange = { ThemePreferences.saveShowWorkoutNotification(context, it) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun GlassSettingContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun ToggleSettingItem(icon: ImageVector, label: String, value: String, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SwitchSettingItem(icon: ImageVector, label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(description, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
