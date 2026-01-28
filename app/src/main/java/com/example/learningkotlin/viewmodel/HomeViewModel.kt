package com.example.learningkotlin.viewmodel

import android.app.Application
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import com.example.learningkotlin.data.WorkoutManager
import com.example.learningkotlin.model.Workout

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // 1. STATE: The list of workouts
    val workouts = WorkoutManager.loadWorkouts(application.applicationContext).toMutableStateList()

    // 2. HELPER: Save to file
    private fun save() = WorkoutManager.saveWorkouts(getApplication(), workouts)

    // 3. EVENT HANDLERS / LOGIC
    fun addWorkout(name: String) {
        val newId = (workouts.maxOfOrNull { it.id } ?: 0) + 1
        workouts.add(Workout(newId, name, mutableListOf()))
        save()
    }

    fun deleteWorkout(workout: Workout) {
        workouts.remove(workout)
        save()
    }

    fun renameWorkout(workout: Workout, newName: String) {
        workouts.find { it.id == workout.id }?.name = newName
        save()
    }

    fun startWorkout(workout: Workout) {
        // IMPROVEMENT: Ensure only ONE workout is active at a time
        workouts.forEach { 
            if (it.isActive) {
                it.isActive = false
                it.startTime = null
            }
        }

        val target = workouts.find { it.id == workout.id }
        target?.let {
            it.isActive = true
            it.startTime = System.currentTimeMillis()
            save()
        }
    }

    fun finishWorkout(workout: Workout) {
        val target = workouts.find { it.id == workout.id }
        target?.let {
            it.isActive = false
            it.startTime = null
            save()
        }
    }

    fun onDetailScreenExit() {
        save()
    }
}