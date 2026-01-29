package com.example.learningkotlin.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningkotlin.data.ExerciseLibrary
import com.example.learningkotlin.data.HistoryManager
import com.example.learningkotlin.data.WorkoutManager
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // 1. STATE: The list of workouts
    val workouts = WorkoutManager.loadWorkouts(application.applicationContext).toMutableStateList()
    
    // History state
    var history = HistoryManager.loadHistory(application.applicationContext).toMutableStateList()
        private set

    // Initialize Exercise Library
    init {
        ExerciseLibrary.load(application.applicationContext)
    }

    // 2. TIMER STATE (Now global to the app)
    var restTimerSeconds by mutableIntStateOf(0)
        private set
    var initialRestTimerSeconds by mutableIntStateOf(0) 
        private set
    var isRestTimerRunning by mutableStateOf(false)
        private set
    private var timerJob: Job? = null

    // For Recap navigation
    var lastFinishedWorkout by mutableStateOf<FinishedWorkout?>(null)
        private set

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

    fun updateFinishedWorkout(updatedWorkout: FinishedWorkout) {
        val index = history.indexOfFirst { it.id == updatedWorkout.id }
        if (index != -1) {
            history[index] = updatedWorkout
            HistoryManager.updateWorkout(getApplication(), updatedWorkout)
        }
    }

    fun deleteFinishedWorkout(workoutId: Int) {
        history.removeAll { it.id == workoutId }
        HistoryManager.deleteWorkout(getApplication(), workoutId)
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
            val now = System.currentTimeMillis()
            val duration = (now - (it.startTime ?: now)) / 1000
            
            // Calculate stats for history
            val totalSets = it.exercises.sumOf { ex -> ex.sets.count { s -> s.isDone } }
            val totalVolume = it.exercises.sumOf { ex -> ex.sets.filter { s -> s.isDone }.sumOf { s -> s.weight * s.reps } }
            
            val finished = FinishedWorkout(
                id = (history.maxOfOrNull { h -> h.id } ?: 0) + 1,
                name = it.name,
                date = now,
                durationSeconds = duration,
                totalVolume = totalVolume,
                totalSets = totalSets,
                exercises = it.exercises.map { ex -> 
                    val def = ExerciseLibrary.getDefinitions().find { d -> d.name == ex.name }
                    ex.copy(
                        sets = ex.sets.filter { s -> s.isDone }.toMutableList(),
                        primaryMuscles = def?.primaryMuscles ?: emptyList()
                    )
                }.filter { ex -> ex.sets.isNotEmpty() }
            )
            
            HistoryManager.saveFinishedWorkout(getApplication(), finished)
            history.add(0, finished)
            lastFinishedWorkout = finished

            // Reset current workout
            it.isActive = false
            it.startTime = null
            // Reset "isDone" for next time
            it.exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
            
            save()
        }
    }

    fun clearLastFinishedWorkout() {
        lastFinishedWorkout = null
    }

    fun isAnyOtherWorkoutActive(currentWorkoutId: Int): Boolean {
        return workouts.any { it.isActive && it.id != currentWorkoutId }
    }

    // --- TIMER LOGIC ---
    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        initialRestTimerSeconds = seconds
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
        initialRestTimerSeconds = 0
        isRestTimerRunning = false
    }

    fun add15Seconds() {
        restTimerSeconds += 15
        initialRestTimerSeconds += 15 
    }

    fun sub15Seconds() {
        if (restTimerSeconds > 15) {
            restTimerSeconds -= 15
        } else {
            skipTimer()
        }
    }

    fun onDetailScreenExit() {
        save()
    }
}