package com.federicocerra.gymlog.data.repository

import android.content.Context
import com.federicocerra.gymlog.data.WorkoutManager
import com.federicocerra.gymlog.model.Workout
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
