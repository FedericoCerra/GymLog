package com.example.learningkotlin.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_PRIMARY_COLOR = "primary_color"
    private const val KEY_WEIGHT_UNIT = "weight_unit"
    private const val KEY_AUTO_REST_TIMER = "auto_rest_timer"
    private const val KEY_TIMER_SOUND = "timer_sound"
    private const val KEY_SHOW_OVERLAY_BUBBLE = "show_overlay_bubble"
    private const val KEY_SHOW_WORKOUT_NOTIFICATION = "show_workout_notification"

    val primaryColor = mutableStateOf(Color(0xFF2196F3))
    val weightUnit = mutableStateOf("kg")
    val autoRestTimer = mutableStateOf(true)
    val timerSound = mutableStateOf(true)
    val showOverlayBubble = mutableStateOf(true)
    val showWorkoutNotification = mutableStateOf(true)

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        val argb = prefs.getInt(KEY_PRIMARY_COLOR, Color(0xFF2196F3).toArgb())
        primaryColor.value = Color(argb)
        
        weightUnit.value = prefs.getString(KEY_WEIGHT_UNIT, "kg") ?: "kg"
        autoRestTimer.value = prefs.getBoolean(KEY_AUTO_REST_TIMER, true)
        timerSound.value = prefs.getBoolean(KEY_TIMER_SOUND, true)
        showOverlayBubble.value = prefs.getBoolean(KEY_SHOW_OVERLAY_BUBBLE, true)
        showWorkoutNotification.value = prefs.getBoolean(KEY_SHOW_WORKOUT_NOTIFICATION, true)
    }

    fun saveColor(context: Context, color: Color) {
        primaryColor.value = color
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_PRIMARY_COLOR, color.toArgb()) }
    }

    fun saveWeightUnit(context: Context, unit: String) {
        weightUnit.value = unit
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_WEIGHT_UNIT, unit) }
    }

    fun saveAutoRestTimer(context: Context, enabled: Boolean) {
        autoRestTimer.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_AUTO_REST_TIMER, enabled) }
    }

    fun saveTimerSound(context: Context, enabled: Boolean) {
        timerSound.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_TIMER_SOUND, enabled) }
    }

    fun saveShowOverlayBubble(context: Context, enabled: Boolean) {
        showOverlayBubble.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOW_OVERLAY_BUBBLE, enabled) }
    }

    fun saveShowWorkoutNotification(context: Context, enabled: Boolean) {
        showWorkoutNotification.value = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOW_WORKOUT_NOTIFICATION, enabled) }
    }

    fun formatWeight(kg: Double): String {
        val isLbs = weightUnit.value == "lbs"
        val value = if (isLbs) kg * 2.20462 else kg
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            "%.1f".format(value)
        }
    }

    fun getWeightSuffix(): String = weightUnit.value
}
