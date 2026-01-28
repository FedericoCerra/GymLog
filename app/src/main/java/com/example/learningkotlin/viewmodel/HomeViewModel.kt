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

    fun startWorkout(workout: Workout): Boolean {
        // Prevent starting if ANY workout is already active
        if (workouts.any { it.isActive }) return false

        val target = workouts.find { it.id == workout.id }
        target?.let {
            it.isActive = true
            it.startTime = System.currentTimeMillis()
            save()
            return true
        }
        return false
    }

    fun finishWorkout(workout: Workout) {
        val target = workouts.find { it.id == workout.id }
        target?.let {
            it.isActive = false
            it.startTime = null
            // Optional: You could reset set.isDone here if you want a fresh start next time
            // it.exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
            save()
        }
    }

    // Returns true if there is a workout active that is NOT the one provided
    fun isAnyOtherWorkoutActive(currentWorkoutId: Int): Boolean {
        return workouts.any { it.isActive && it.id != currentWorkoutId }
    }

    fun onDetailScreenExit() {
        save()
    }
}