package com.federicocerra.gymlog.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.federicocerra.gymlog.data.ExerciseLibrary
import com.federicocerra.gymlog.data.ThemePreferences
import com.federicocerra.gymlog.data.repository.HistoryRepository
import com.federicocerra.gymlog.data.repository.WorkoutRepository
import com.federicocerra.gymlog.model.FinishedWorkout
import com.federicocerra.gymlog.model.Workout
import com.federicocerra.gymlog.model.WorkoutSet
import com.federicocerra.gymlog.service.WorkoutOverlayService
import kotlinx.coroutines.launch

data class ExercisePersonalBests(
    val maxWeight: Double = 0.0,
    val max1RM: Double = 0.0,
    val maxVolume: Double = 0.0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val workoutRepository = WorkoutRepository(application)
    private val historyRepository = HistoryRepository(application)

    // 1. STATE
    var workouts by mutableStateOf<List<Workout>>(emptyList())
        private set
    var history by mutableStateOf<List<FinishedWorkout>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    
    // Bridge to Service state
    val restTimerSeconds get() = WorkoutOverlayService.restTimerSeconds.intValue
    val isRestTimerRunning get() = WorkoutOverlayService.isTimerRunning.value
    
    var initialRestTimerSeconds by mutableIntStateOf(0) 
        private set

    var lastFinishedWorkout by mutableStateOf<FinishedWorkout?>(null)
        private set

    init {
        ExerciseLibrary.load(application)
        viewModelScope.launch {
            ThemePreferences.load(application)
        }
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            isLoading = true
            workouts = workoutRepository.getWorkouts()
            history = historyRepository.getHistory()
            
            // Resume overlay if a workout was active
            workouts.find { it.isActive }?.let { active ->
                WorkoutOverlayService.start(getApplication(), active.name, active.startTime ?: System.currentTimeMillis(), active.id)
            }
            isLoading = false
        }
    }

    fun clearData() {
        workouts = emptyList()
        history = emptyList()
        skipTimer()
        lastFinishedWorkout = null
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

        val startTime = System.currentTimeMillis()
        workouts = workouts.map {
            if (it.id == workout.id) it.copy(isActive = true, startTime = startTime) else it
        }
        saveRoutines()
        
        // Start system-wide overlay
        WorkoutOverlayService.start(getApplication(), workout.name, startTime, workout.id)
        
        return true
    }

    fun discardWorkout(workout: Workout) {
        val target = workouts.find { it.id == workout.id } ?: return
        
        workouts = workouts.map {
            if (it.id == target.id) {
                it.copy(isActive = false, startTime = null).apply {
                    exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
                }
            } else it
        }
        skipTimer()
        saveRoutines()
        
        // Stop system-wide overlay
        WorkoutOverlayService.stop(getApplication())
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
                
                // Final PR calculation for history persistence
                val historicalBests = getPersonalBests(ex.name)
                val doneSets = ex.sets.filter { s -> s.isDone }
                
                val sessionBestWeight = doneSets.maxOfOrNull { it.weight } ?: -1.0
                val sessionBest1RM = doneSets.maxOfOrNull { it.calculate1RM() } ?: -1.0
                val sessionBestVolume = doneSets.maxOfOrNull { it.calculateVolume() } ?: -1.0
                
                // Identify the IDs of the LAST occurring session bests
                val lastBestWeightId = doneSets.findLast { it.weight == sessionBestWeight }?.id ?: -1
                val lastBest1RMId = doneSets.findLast { it.calculate1RM() == sessionBest1RM }?.id ?: -1
                val lastBestVolumeId = doneSets.findLast { it.calculateVolume() == sessionBestVolume }?.id ?: -1

                ex.copy(
                    sets = doneSets.map { s ->
                        s.copy(
                            isWeightPR = s.id == lastBestWeightId && s.weight > 0 && (historicalBests.maxWeight == 0.0 || s.weight > historicalBests.maxWeight),
                            is1RMPR = s.id == lastBest1RMId && s.calculate1RM() > 0 && (historicalBests.max1RM == 0.0 || s.calculate1RM() > historicalBests.max1RM),
                            isVolumePR = s.id == lastBestVolumeId && s.calculateVolume() > 0 && (historicalBests.maxVolume == 0.0 || s.calculateVolume() > historicalBests.maxVolume)
                        )
                    }.toMutableList(),
                    primaryMuscles = def?.primaryMuscles ?: emptyList()
                )
            }.filter { ex -> ex.sets.isNotEmpty() }
        )
        
        viewModelScope.launch {
            historyRepository.saveFinishedWorkout(finished)
        }
        history = listOf(finished) + history
        lastFinishedWorkout = finished

        skipTimer()

        workouts = workouts.map {
            if (it.id == target.id) {
                it.copy(isActive = false, startTime = null).apply {
                    exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
                }
            } else it
        }
        saveRoutines()
        
        // Stop system-wide overlay
        WorkoutOverlayService.stop(getApplication())
    }

    fun isAnyOtherWorkoutActive(currentWorkoutId: Int): Boolean {
        return workouts.any { it.isActive && it.id != currentWorkoutId }
    }

    fun getPreviousSetsForExercise(exerciseName: String): List<WorkoutSet> {
        val lastWorkoutWithExercise = history.sortedByDescending { it.date }
            .firstOrNull { workout -> 
                workout.exercises.any { it.name == exerciseName } 
            }
        
        return lastWorkoutWithExercise?.exercises?.find { it.name == exerciseName }?.sets ?: emptyList()
    }

    fun getPersonalBests(exerciseName: String): ExercisePersonalBests {
        val historicalExercises = history.flatMap { it.exercises }.filter { it.name == exerciseName }
        val historicalSets = historicalExercises.flatMap { it.sets }
        
        return ExercisePersonalBests(
            maxWeight = historicalSets.maxOfOrNull { it.weight } ?: 0.0,
            max1RM = historicalSets.maxOfOrNull { it.calculate1RM() } ?: 0.0,
            maxVolume = historicalSets.maxOfOrNull { it.calculateVolume() } ?: 0.0
        )
    }

    // 5. TIMER LOGIC
    fun onSetChecked(seconds: Int) {
        if (ThemePreferences.autoRestTimer.value) {
            startRestTimer(seconds)
        }
    }

    fun startRestTimer(seconds: Int) {
        initialRestTimerSeconds = seconds
        WorkoutOverlayService.requestStartTimer(getApplication(), seconds)
    }

    fun skipTimer() {
        initialRestTimerSeconds = 0
        WorkoutOverlayService.requestStopTimer(getApplication())
    }

    fun add15Seconds() {
        initialRestTimerSeconds += 15
        WorkoutOverlayService.requestUpdateTimer(getApplication(), 15)
    }

    fun sub15Seconds() {
        if (restTimerSeconds > 15) {
            initialRestTimerSeconds -= 15
        } else {
            initialRestTimerSeconds = 0
        }
        WorkoutOverlayService.requestUpdateTimer(getApplication(), -15)
    }

    fun onDetailScreenExit() {
        saveRoutines()
    }
}
