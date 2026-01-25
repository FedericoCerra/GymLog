package com.example.learningkotlin.data

import android.content.Context
import com.example.learningkotlin.model.Workout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object WorkoutManager {
    private const val FILE_NAME = "my_workouts.json"

    // Save the list to a file
    fun saveWorkouts(context: Context, workouts: List<Workout>) {
        try {
            val jsonString = Json.encodeToString(workouts)
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load the list from the file
    fun loadWorkouts(context: Context): MutableList<Workout> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()

        return try {
            val jsonString = file.readText()
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }
}