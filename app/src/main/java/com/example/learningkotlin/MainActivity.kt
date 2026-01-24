package com.example.learningkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.navigation.SetupNavGraph
import com.example.learningkotlin.ui.theme.LearningKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearningKotlinTheme {
                // 1. Create the Navigation Controller
                val navController = rememberNavController()

                // 2. Create the Data (State)
                // We use 'remember' so the list survives when screen rotates
                // Inside MainActivity.kt

                val workouts = remember {
                    mutableStateListOf(
                        Workout(
                            id = 1,
                            name = "Push Day",
                            exercises = mutableListOf(
                                Exercise(
                                    id = 1,
                                    name = "Bench Press",
                                    sets = mutableListOf(
                                        WorkoutSet(1, 40.0, 10, true), // Set 1: Done
                                        WorkoutSet(2, 60.0, 8, false), // Set 2: Not done
                                        WorkoutSet(3, 80.0, 5, false)  // Set 3: Not done
                                    )
                                ),
                                Exercise(
                                    id = 2,
                                    name = "Incline Dumbbell",
                                    sets = mutableListOf(
                                        WorkoutSet(1, 20.0, 12, false),
                                        WorkoutSet(2, 20.0, 10, false)
                                    )
                                )
                            )
                        )
                        // ... add more workouts if you want
                    )
                }
                // 3. Start the App via Navigation
                SetupNavGraph(
                    navController = navController,
                    workouts = workouts,
                    onAddFunc = {
                        // LOGIC: Create a new empty workout with a unique ID
                        val newId = (workouts.maxOfOrNull { it.id } ?: 0) + 1
                        workouts.add(
                            Workout(newId, "New Workout $newId", mutableListOf())
                        )
                    },
                    onDeleteFunc = { workoutToRemove ->
                        workouts.remove(workoutToRemove)
                    }
                )
            }
        }
    }
}