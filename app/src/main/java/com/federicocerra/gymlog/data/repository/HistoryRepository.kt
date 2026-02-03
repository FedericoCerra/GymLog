package com.federicocerra.gymlog.data.repository

import android.content.Context
import com.federicocerra.gymlog.data.HistoryManager
import com.federicocerra.gymlog.model.FinishedWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val context: Context) {

    suspend fun getHistory(): List<FinishedWorkout> = withContext(Dispatchers.IO) {
        HistoryManager.loadHistory(context)
    }

    suspend fun saveFinishedWorkout(workout: FinishedWorkout) = withContext(Dispatchers.IO) {
        HistoryManager.saveFinishedWorkout(context, workout)
    }

    suspend fun updateWorkout(workout: FinishedWorkout) = withContext(Dispatchers.IO) {
        HistoryManager.updateWorkout(context, workout)
    }

    suspend fun deleteWorkout(workoutId: Int) = withContext(Dispatchers.IO) {
        HistoryManager.deleteWorkout(context, workoutId)
    }
}
