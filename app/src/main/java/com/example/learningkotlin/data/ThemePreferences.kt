package com.example.learningkotlin.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_PRIMARY_COLOR = "primary_color"

    val primaryColor = mutableStateOf(Color(0xFF2196F3)) // Default HevyBlue

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val argb = prefs.getInt(KEY_PRIMARY_COLOR, Color(0xFF2196F3).toArgb())
        primaryColor.value = Color(argb)
    }

    fun save(context: Context, color: Color) {
        primaryColor.value = color
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_PRIMARY_COLOR, color.toArgb()).apply()
    }
}
