package com.federicocerra.gymlog.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.federicocerra.gymlog.data.ThemePreferences

@Composable
fun LearningKotlinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val currentPrimary = ThemePreferences.primaryColor.value

    val darkColorScheme = darkColorScheme(
        primary = currentPrimary,
        onPrimary = Color.White,
        background = HevyBlack,
        onBackground = HevyWhite,
        surface = HevyBlack,
        surfaceVariant = HevyDarkGrey,
        onSurface = HevyWhite,
        onSurfaceVariant = HevyLightGrey,
        secondaryContainer = HevyInputGrey,
        onSecondaryContainer = HevyWhite,
        error = HevyRed
    )

    val lightColorScheme = lightColorScheme(
        primary = currentPrimary,
        onPrimary = Color.White,
        background = Color(0xFFF2F2F7),
        onBackground = Color.Black,
        surface = Color.White,
        surfaceVariant = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = Color.Gray,
        secondaryContainer = Color(0xFFE5E5EA),
        onSecondaryContainer = Color.Black
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
