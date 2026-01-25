package com.example.learningkotlin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define the Dark Theme (The one you want)
private val DarkColorScheme = darkColorScheme(
    primary = HevyBlue,
    onPrimary = Color.White,

    // Backgrounds
    background = HevyBlack,
    onBackground = HevyWhite,

    // Cards
    surface = HevyBlack,         // Main screen background
    surfaceVariant = HevyDarkGrey, // Card background
    onSurface = HevyWhite,       // Text on cards
    onSurfaceVariant = HevyLightGrey, // Subtext on cards

    // Inputs
    secondaryContainer = HevyInputGrey,
    onSecondaryContainer = HevyWhite,

    error = HevyRed
)

// Define Light Theme (Just in case, but keeping it blue/clean)
private val LightColorScheme = lightColorScheme(
    primary = HevyBlue,
    onPrimary = Color.White,

    background = Color(0xFFF2F2F7), // Light iOS grey style
    onBackground = Color.Black,

    surface = Color.White,
    surfaceVariant = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray,

    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = Color.Black
)

@Composable
fun LearningKotlinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANT: We set this to FALSE to ignore the user's wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make the status bar (battery, time) match the background
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Keep default typography for now
        content = content
    )
}