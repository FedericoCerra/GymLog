package com.example.learningkotlin.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ThemePreferences {
    private fun getPrefsName(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return "theme_prefs_$userId"
    }

    private val db = FirebaseFirestore.getInstance()

    private const val KEY_PRIMARY_COLOR = "primary_color"
    private const val KEY_WEIGHT_UNIT = "weight_unit"
    private const val KEY_AUTO_REST_TIMER = "auto_rest_timer"
    private const val KEY_TIMER_SOUND = "timer_sound"
    private const val KEY_SHOW_OVERLAY_BUBBLE = "show_overlay_bubble"
    private const val KEY_SHOW_WORKOUT_NOTIFICATION = "show_workout_notification"
    private const val KEY_LAST_UPDATED = "last_updated"
    private const val KEY_USER_EMAIL = "user_email"

    val primaryColor = mutableStateOf(Color(0xFF2196F3))
    val weightUnit = mutableStateOf("kg")
    val autoRestTimer = mutableStateOf(true)
    val timerSound = mutableStateOf(true)
    val showOverlayBubble = mutableStateOf(true)
    val showWorkoutNotification = mutableStateOf(true)

    fun reset() {
        primaryColor.value = Color(0xFF2196F3)
        weightUnit.value = "kg"
        autoRestTimer.value = true
        timerSound.value = true
        showOverlayBubble.value = true
        showWorkoutNotification.value = true
    }

    suspend fun load(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        
        var localTime = prefs.getLong(KEY_LAST_UPDATED, 0L)

        if (userId != null) {
            try {
                val doc = db.collection("users").document(userId)
                    .collection("data").document("settings")
                    .get()
                    .await()

                if (doc.exists()) {
                    val remoteTime = doc.getLong(KEY_LAST_UPDATED) ?: 0L
                    if (remoteTime > localTime) {
                        // Sync remote to local
                        val color = doc.getLong(KEY_PRIMARY_COLOR)?.toInt() ?: Color(0xFF2196F3).toArgb()
                        val unit = doc.getString(KEY_WEIGHT_UNIT) ?: "kg"
                        val autoTimer = doc.getBoolean(KEY_AUTO_REST_TIMER) ?: true
                        val sound = doc.getBoolean(KEY_TIMER_SOUND) ?: true
                        val bubble = doc.getBoolean(KEY_SHOW_OVERLAY_BUBBLE) ?: true
                        val notification = doc.getBoolean(KEY_SHOW_WORKOUT_NOTIFICATION) ?: true
                        
                        prefs.edit {
                            putInt(KEY_PRIMARY_COLOR, color)
                            putString(KEY_WEIGHT_UNIT, unit)
                            putBoolean(KEY_AUTO_REST_TIMER, autoTimer)
                            putBoolean(KEY_TIMER_SOUND, sound)
                            putBoolean(KEY_SHOW_OVERLAY_BUBBLE, bubble)
                            putBoolean(KEY_SHOW_WORKOUT_NOTIFICATION, notification)
                            putLong(KEY_LAST_UPDATED, remoteTime)
                        }
                        localTime = remoteTime
                    } else if (localTime >= remoteTime) {
                        // Local is newer or same, push to remote (ensures email is set)
                        saveToFirestore(context)
                    }
                } else {
                    // No remote data, push local
                    saveToFirestore(context)
                }
            } catch (e: Exception) { }
        }

        // Apply values to state
        val argb = prefs.getInt(KEY_PRIMARY_COLOR, Color(0xFF2196F3).toArgb())
        primaryColor.value = Color(argb)
        weightUnit.value = prefs.getString(KEY_WEIGHT_UNIT, "kg") ?: "kg"
        autoRestTimer.value = prefs.getBoolean(KEY_AUTO_REST_TIMER, true)
        timerSound.value = prefs.getBoolean(KEY_TIMER_SOUND, true)
        showOverlayBubble.value = prefs.getBoolean(KEY_SHOW_OVERLAY_BUBBLE, true)
        showWorkoutNotification.value = prefs.getBoolean(KEY_SHOW_WORKOUT_NOTIFICATION, true)
    }

    private fun saveToFirestore(context: Context) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        
        val data = mapOf(
            KEY_USER_EMAIL to (user.email ?: "unknown"),
            KEY_PRIMARY_COLOR to prefs.getInt(KEY_PRIMARY_COLOR, primaryColor.value.toArgb()),
            KEY_WEIGHT_UNIT to (prefs.getString(KEY_WEIGHT_UNIT, weightUnit.value) ?: "kg"),
            KEY_AUTO_REST_TIMER to prefs.getBoolean(KEY_AUTO_REST_TIMER, autoRestTimer.value),
            KEY_TIMER_SOUND to prefs.getBoolean(KEY_TIMER_SOUND, timerSound.value),
            KEY_SHOW_OVERLAY_BUBBLE to prefs.getBoolean(KEY_SHOW_OVERLAY_BUBBLE, showOverlayBubble.value),
            KEY_SHOW_WORKOUT_NOTIFICATION to prefs.getBoolean(KEY_SHOW_WORKOUT_NOTIFICATION, showWorkoutNotification.value),
            KEY_LAST_UPDATED to prefs.getLong(KEY_LAST_UPDATED, System.currentTimeMillis())
        )
        
        db.collection("users").document(userId)
            .collection("data").document("settings")
            .set(data)
    }

    private fun updateLocalTime(context: Context): Long {
        val time = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_UPDATED, time) }
        return time
    }

    fun saveColor(context: Context, color: Color) {
        primaryColor.value = color
        val time = updateLocalTime(context)
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_PRIMARY_COLOR, color.toArgb()) }
        saveToFirestore(context)
    }

    fun saveWeightUnit(context: Context, unit: String) {
        weightUnit.value = unit
        val time = updateLocalTime(context)
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_WEIGHT_UNIT, unit) }
        saveToFirestore(context)
    }

    fun saveAutoRestTimer(context: Context, enabled: Boolean) {
        autoRestTimer.value = enabled
        val time = updateLocalTime(context)
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_AUTO_REST_TIMER, enabled) }
        saveToFirestore(context)
    }

    fun saveTimerSound(context: Context, enabled: Boolean) {
        timerSound.value = enabled
        val time = updateLocalTime(context)
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_TIMER_SOUND, enabled) }
        saveToFirestore(context)
    }

    fun saveShowOverlayBubble(context: Context, enabled: Boolean) {
        showOverlayBubble.value = enabled
        val time = updateLocalTime(context)
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOW_OVERLAY_BUBBLE, enabled) }
        saveToFirestore(context)
    }

    fun saveShowWorkoutNotification(context: Context, enabled: Boolean) {
        showWorkoutNotification.value = enabled
        val time = updateLocalTime(context)
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOW_WORKOUT_NOTIFICATION, enabled) }
        saveToFirestore(context)
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
