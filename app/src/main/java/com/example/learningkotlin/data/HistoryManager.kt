package com.example.learningkotlin.data

import android.content.Context
import com.example.learningkotlin.model.FinishedWorkout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object HistoryManager {
    private const val FILE_NAME = "workout_history.json"
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
        encodeDefaults = true
    }

    fun saveFinishedWorkout(context: Context, workout: FinishedWorkout) {
        val history = loadHistory(context).toMutableList()
        history.add(0, workout)
        saveHistory(context, history)
    }

    fun updateWorkout(context: Context, updatedWorkout: FinishedWorkout) {
        val history = loadHistory(context).toMutableList()
        val index = history.indexOfFirst { it.id == updatedWorkout.id }
        if (index != -1) {
            history[index] = updatedWorkout
            saveHistory(context, history)
        }
    }

    fun deleteWorkout(context: Context, workoutId: Int) {
        val history = loadHistory(context).toMutableList()
        history.removeAll { it.id == workoutId }
        saveHistory(context, history)
    }

    private fun saveHistory(context: Context, history: List<FinishedWorkout>) {
        try {
            val jsonString = json.encodeToString(history)
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadHistory(context: Context): List<FinishedWorkout> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val jsonString = file.readText()
            json.decodeFromString<List<FinishedWorkout>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
