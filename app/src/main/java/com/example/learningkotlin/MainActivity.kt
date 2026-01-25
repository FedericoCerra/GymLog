package com.example.learningkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.learningkotlin.data.WorkoutManager
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.navigation.NavGraph // Import your file
import com.example.learningkotlin.ui.theme.LearningKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearningKotlinTheme(dynamicColor = false) {
                val context = LocalContext.current
                val navController = rememberNavController()

                // 1. DATA: Load from file
                val workouts = remember {
                    WorkoutManager.loadWorkouts(context).toMutableStateList()
                }

                // 2. LOGIC: Helper to save data
                fun save() = WorkoutManager.saveWorkouts(context, workouts)

                // 3. NAVIGATION: Hand off to your NavGraph file
                NavGraph(
                    navController = navController,
                    workouts = workouts,
                    onSave = { save() },

                    // CHANGED: Now we receive the 'name' from the dialog
                    onAddWorkout = { name ->
                        val nextId = (workouts.maxOfOrNull { it.id } ?: 0) + 1
                        val newWorkout = Workout(
                            id = nextId,
                            name = name, // <--- Use the typed name!
                            exercises = mutableListOf()
                        )
                        workouts.add(newWorkout)
                        save()
                    },

                    onDeleteWorkout = { workout ->
                        workouts.remove(workout)
                        save()
                    }
                )
            }
        }
    }
}