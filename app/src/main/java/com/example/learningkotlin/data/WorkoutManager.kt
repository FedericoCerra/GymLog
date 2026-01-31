package com.example.learningkotlin.data

import android.content.Context
import com.example.learningkotlin.model.Workout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object WorkoutManager {
    private fun getFileName(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return "my_workouts_$userId.json"
    }

    // Save the list to a file
    fun saveWorkouts(context: Context, workouts: List<Workout>) {
        try {
            val jsonString = Json.encodeToString(workouts)
            val file = File(context.filesDir, getFileName())
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load the list from the file
    fun loadWorkouts(context: Context): List<Workout> {
        val file = File(context.filesDir, getFileName())
        if (!file.exists()) return emptyList()

        return try {
            val jsonString = file.readText()
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
