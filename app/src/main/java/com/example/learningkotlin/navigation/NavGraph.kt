package com.example.learningkotlin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.learningkotlin.model.Workout
import com.example.learningkotlin.ui.screens.HomeScreen
import com.example.learningkotlin.ui.screens.WorkoutDetailScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    workouts: MutableList<Workout>,      // The Data
    onAddWorkout: () -> Unit,            // Action: Add Button clicked
    onDeleteWorkout: (Workout) -> Unit,  // Action: Delete clicked
    onSave: () -> Unit                   // Action: Save to file
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // --- SCREEN 1: HOME ---
        composable("home") {
            HomeScreen(
                workouts = workouts,
                onAddClick = onAddWorkout,
                onDeleteClick = onDeleteWorkout,
                onWorkoutClick = { workout ->
                    // Navigate to Detail using the ID
                    navController.navigate("detail/${workout.id}")
                }
            )
        }

        // --- SCREEN 2: DETAIL ---
        composable(
            route = "detail/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { backStackEntry ->
            // 1. Get ID from URL
            val workoutId = backStackEntry.arguments?.getInt("workoutId")

            // 2. Find the specific workout object
            val selectedWorkout = workouts.find { it.id == workoutId }

            // 3. Show Screen
            if (selectedWorkout != null) {
                WorkoutDetailScreen(
                    workout = selectedWorkout,
                    onBackClick = {
                        onSave() // <--- Auto-save when going back
                        navController.popBackStack()
                    },
                    onAddExerciseClick = {
                        // Handled internally by the screen now
                    }
                )
            }
        }
    }
}