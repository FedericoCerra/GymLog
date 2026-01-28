package com.example.learningkotlin.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningkotlin.data.WorkoutManager
import com.example.learningkotlin.model.Workout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // 1. STATE: The list of workouts
    val workouts = WorkoutManager.loadWorkouts(application.applicationContext).toMutableStateList()

    // 2. TIMER STATE (Now global to the app)
    var restTimerSeconds by mutableIntStateOf(0)
        private set
    var isRestTimerRunning by mutableStateOf(false)
        private set
    private var timerJob: Job? = null

    // 3. HELPER: Save to file
    private fun save() = WorkoutManager.saveWorkouts(getApplication(), workouts)

    // 4. EVENT HANDLERS / LOGIC
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
            save()
        }
    }

    fun isAnyOtherWorkoutActive(currentWorkoutId: Int): Boolean {
        return workouts.any { it.isActive && it.id != currentWorkoutId }
    }

    // --- TIMER LOGIC ---
    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        restTimerSeconds = seconds
        isRestTimerRunning = true
        
        timerJob = viewModelScope.launch {
            while (restTimerSeconds > 0) {
                delay(1000L)
                restTimerSeconds -= 1
            }
            isRestTimerRunning = false
        }
    }

    fun skipTimer() {
        timerJob?.cancel()
        restTimerSeconds = 0
        isRestTimerRunning = false
    }

    fun add15Seconds() {
        restTimerSeconds += 15
    }

    fun sub15Seconds() {
        if (restTimerSeconds > 15) restTimerSeconds -= 15 else skipTimer()
    }

    fun onDetailScreenExit() {
        save()
    }
}