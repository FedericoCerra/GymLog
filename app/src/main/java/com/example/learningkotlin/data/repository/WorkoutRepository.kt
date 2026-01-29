package com.example.learningkotlin.data.repository

import android.content.Context
import com.example.learningkotlin.data.WorkoutManager
import com.example.learningkotlin.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepository(private val context: Context) {

    suspend fun getWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
        WorkoutManager.loadWorkouts(context)
    }

    suspend fun saveWorkouts(workouts: List<Workout>) = withContext(Dispatchers.IO) {
        WorkoutManager.saveWorkouts(context, workouts)
    }
}
