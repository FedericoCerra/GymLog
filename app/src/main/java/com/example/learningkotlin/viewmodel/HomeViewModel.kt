package com.example.learningkotlin.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningkotlin.data.ExerciseLibrary
import com.example.learningkotlin.data.repository.HistoryRepository
import com.example.learningkotlin.data.repository.WorkoutRepository
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.model.Workout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val workoutRepository = WorkoutRepository(application)
    private val historyRepository = HistoryRepository(application)

    // 1. STATE - Using mutableStateOf<List> for better reactivity on whole-list updates
    var workouts by mutableStateOf<List<Workout>>(emptyList())
        private set
    var history by mutableStateOf<List<FinishedWorkout>>(emptyList())
        private set
    
    var restTimerSeconds by mutableIntStateOf(0)
        private set
    var initialRestTimerSeconds by mutableIntStateOf(0) 
        private set
    var isRestTimerRunning by mutableStateOf(false)
        private set
    private var timerJob: Job? = null

    var lastFinishedWorkout by mutableStateOf<FinishedWorkout?>(null)
        private set

    init {
        ExerciseLibrary.load(application)
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            workouts = workoutRepository.getWorkouts()
            history = historyRepository.getHistory()
        }
    }

    fun saveRoutines() {
        viewModelScope.launch {
            workoutRepository.saveWorkouts(workouts)
        }
    }

    // 2. ROUTINE CRUD
    fun addWorkout(name: String) {
        val newId = (workouts.maxOfOrNull { it.id } ?: 0) + 1
        workouts = workouts + Workout(newId, name, mutableListOf())
        saveRoutines()
    }

    fun deleteWorkout(workout: Workout) {
        workouts = workouts.filter { it.id != workout.id }
        saveRoutines()
    }

    fun renameWorkout(workout: Workout, newName: String) {
        workouts = workouts.map { 
            if (it.id == workout.id) it.copy(name = newName) else it 
        }
        saveRoutines()
    }

    // 3. HISTORY CRUD
    fun updateFinishedWorkout(updatedWorkout: FinishedWorkout) {
        history = history.map {
            if (it.id == updatedWorkout.id) updatedWorkout else it
        }
        viewModelScope.launch {
            historyRepository.updateWorkout(updatedWorkout)
        }
    }

    fun deleteFinishedWorkout(workoutId: Int) {
        history = history.filter { it.id != workoutId }
        viewModelScope.launch {
            historyRepository.deleteWorkout(workoutId)
        }
    }

    // 4. WORKOUT SESSION LOGIC
    fun startWorkout(workout: Workout): Boolean {
        if (workouts.any { it.isActive }) return false

        workouts = workouts.map {
            if (it.id == workout.id) it.copy(isActive = true, startTime = System.currentTimeMillis()) else it
        }
        saveRoutines()
        return true
    }

    fun finishWorkout(workout: Workout) {
        val target = workouts.find { it.id == workout.id } ?: return
        
        val now = System.currentTimeMillis()
        val duration = (now - (target.startTime ?: now)) / 1000
        
        val totalSets = target.exercises.sumOf { ex -> ex.sets.count { s -> s.isDone } }
        val totalVolume = target.exercises.sumOf { ex -> ex.sets.filter { s -> s.isDone }.sumOf { s -> s.weight * s.reps } }
        
        val finished = FinishedWorkout(
            id = (history.maxOfOrNull { h -> h.id } ?: 0) + 1,
            name = target.name,
            date = now,
            durationSeconds = duration,
            totalVolume = totalVolume,
            totalSets = totalSets,
            exercises = target.exercises.map { ex -> 
                val def = ExerciseLibrary.getDefinitions().find { d -> d.name == ex.name }
                ex.copy(
                    sets = ex.sets.filter { s -> s.isDone }.toMutableList(),
                    primaryMuscles = def?.primaryMuscles ?: emptyList()
                )
            }.filter { ex -> ex.sets.isNotEmpty() }
        )
        
        viewModelScope.launch {
            historyRepository.saveFinishedWorkout(finished)
        }
        history = listOf(finished) + history
        lastFinishedWorkout = finished

        // Reset current workout in the list
        workouts = workouts.map {
            if (it.id == target.id) {
                it.copy(isActive = false, startTime = null).apply {
                    exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
                }
            } else it
        }
        saveRoutines()
    }

    fun isAnyOtherWorkoutActive(currentWorkoutId: Int): Boolean {
        return workouts.any { it.isActive && it.id != currentWorkoutId }
    }

    // 5. TIMER LOGIC
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
        saveRoutines()
    }
}
