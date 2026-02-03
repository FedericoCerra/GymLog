package com.federicocerra.gymlog.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.federicocerra.gymlog.R
import com.federicocerra.gymlog.data.ExerciseLibrary
import com.federicocerra.gymlog.data.ThemePreferences
import com.federicocerra.gymlog.data.repository.HistoryRepository
import com.federicocerra.gymlog.data.repository.WorkoutRepository
import com.federicocerra.gymlog.model.FinishedWorkout
import com.federicocerra.gymlog.model.Workout
import com.federicocerra.gymlog.model.WorkoutSet
import com.federicocerra.gymlog.service.WorkoutOverlayService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        viewModelScope.launch {
            ThemePreferences.load(application)
        }
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            workouts = workoutRepository.getWorkouts()
            history = historyRepository.getHistory()
            
            // Resume overlay if a workout was active
            workouts.find { it.isActive }?.let { active ->
                WorkoutOverlayService.start(getApplication(), active.name, active.startTime ?: System.currentTimeMillis(), active.id)
            }
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
        timerJob?.cancel()
        initialRestTimerSeconds = seconds
        restTimerSeconds = seconds
        isRestTimerRunning = true
        WorkoutOverlayService.updateTimer(restTimerSeconds, true)
        
        timerJob = viewModelScope.launch {
            while (restTimerSeconds > 0) {
                delay(1000L)
                restTimerSeconds -= 1
                WorkoutOverlayService.updateTimer(restTimerSeconds, true)
            }
            isRestTimerRunning = false
            WorkoutOverlayService.updateTimer(0, false)
            
            // Play sound and vibrate if enabled
            if (ThemePreferences.timerSound.value) {
                triggerTimerAlert()
            }
        }
    }

    private fun triggerTimerAlert() {
        val context = getApplication<Application>()
        
        // 1. Play Custom Bell Sound (res/raw/bell_notification.mp3)
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.bell_notification)
            mediaPlayer.setVolume(0.4f, 0.4f)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mediaPlayer.setOnCompletionListener { it.release() }
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Vibrate
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // More noticeable pattern: [delay, vibrate, sleep, vibrate]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun skipTimer() {
        timerJob?.cancel()
        restTimerSeconds = 0
        initialRestTimerSeconds = 0
        isRestTimerRunning = false
        WorkoutOverlayService.updateTimer(0, false)
    }

    fun add15Seconds() {
        restTimerSeconds += 15
        initialRestTimerSeconds += 15
        WorkoutOverlayService.updateTimer(restTimerSeconds, isRestTimerRunning)
    }

    fun sub15Seconds() {
        if (restTimerSeconds > 15) {
            restTimerSeconds -= 15
            WorkoutOverlayService.updateTimer(restTimerSeconds, isRestTimerRunning)
        } else if (isRestTimerRunning) {
            // Trigger alert if it was running and we subbed below 0
            restTimerSeconds = 0
            isRestTimerRunning = false
            timerJob?.cancel()
            WorkoutOverlayService.updateTimer(0, false)
            if (ThemePreferences.timerSound.value) {
                triggerTimerAlert()
            }
        } else {
            skipTimer()
        }
    }

    fun onDetailScreenExit() {
        saveRoutines()
    }
}
