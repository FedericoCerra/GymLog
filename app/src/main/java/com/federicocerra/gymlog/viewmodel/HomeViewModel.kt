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

    // 1. STATE - Using SnapshotStateList for better reordering support
    private val _workouts = mutableStateListOf<Workout>()
    val workouts: List<Workout> get() = _workouts

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
            val loadedWorkouts = workoutRepository.getWorkouts()
            _workouts.clear()
            _workouts.addAll(loadedWorkouts)
            
            history = historyRepository.getHistory()
            
            // Resume overlay if a workout was active
            _workouts.find { it.isActive }?.let { active ->
                WorkoutOverlayService.start(getApplication(), active.name, active.startTime ?: System.currentTimeMillis(), active.id)
            }
            isLoading = false
        }
    }

    fun clearData() {
        _workouts.clear()
        history = emptyList()
        skipTimer()
        lastFinishedWorkout = null
    }

    fun saveRoutines() {
        viewModelScope.launch {
            workoutRepository.saveWorkouts(_workouts.toList())
        }
    }

    fun moveWorkout(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _workouts.indices || toIndex !in _workouts.indices) return
        val item = _workouts.removeAt(fromIndex)
        _workouts.add(toIndex, item)
        // We don't save on every tiny movement to avoid spamming Firebase,
        // but the UI will be reactive. We should call saveRoutines when drag ends.
    }

    fun onMoveEnd() {
        saveRoutines()
    }

    // 2. ROUTINE CRUD
    fun addWorkout(name: String) {
        val newId = (_workouts.maxOfOrNull { it.id } ?: 0) + 1
        _workouts.add(Workout(newId, name, mutableListOf()))
        saveRoutines()
    }

    fun deleteWorkout(workout: Workout) {
        _workouts.removeAll { it.id == workout.id }
        saveRoutines()
    }

    fun renameWorkout(workout: Workout, newName: String) {
        val index = _workouts.indexOfFirst { it.id == workout.id }
        if (index != -1) {
            _workouts[index] = _workouts[index].copy(name = newName)
            saveRoutines()
        }
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
        if (_workouts.any { it.isActive }) return false

        val startTime = System.currentTimeMillis()
        val index = _workouts.indexOfFirst { it.id == workout.id }
        if (index != -1) {
            _workouts[index] = _workouts[index].copy(isActive = true, startTime = startTime)
            saveRoutines()
            WorkoutOverlayService.start(getApplication(), workout.name, startTime, workout.id)
            return true
        }
        return false
    }

    fun discardWorkout(workout: Workout) {
        val index = _workouts.indexOfFirst { it.id == workout.id }
        if (index != -1) {
            val target = _workouts[index]
            _workouts[index] = target.copy(isActive = false, startTime = null).apply {
                exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
            }
            skipTimer()
            saveRoutines()
            WorkoutOverlayService.stop(getApplication())
        }
    }

    fun finishWorkout(workout: Workout) {
        val index = _workouts.indexOfFirst { it.id == workout.id }
        if (index == -1) return
        val target = _workouts[index]
        
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
                val historicalBests = getPersonalBests(ex.name)
                val doneSets = ex.sets.filter { s -> s.isDone }
                
                val sessionBestWeight = doneSets.maxOfOrNull { it.weight } ?: -1.0
                val sessionBest1RM = doneSets.maxOfOrNull { it.calculate1RM() } ?: -1.0
                val sessionBestVolume = doneSets.maxOfOrNull { it.calculateVolume() } ?: -1.0
                
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

        _workouts[index] = target.copy(isActive = false, startTime = null).apply {
            exercises.forEach { ex -> ex.sets.forEach { s -> s.isDone = false } }
        }
        saveRoutines()
        WorkoutOverlayService.stop(getApplication())
    }

    fun isAnyOtherWorkoutActive(currentWorkoutId: Int): Boolean {
        return _workouts.any { it.isActive && it.id != currentWorkoutId }
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
