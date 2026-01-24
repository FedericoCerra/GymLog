package com.example.learningkotlin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.learningkotlin.model.Workout // Import your model!
import com.example.learningkotlin.ui.screens.HomeScreen
import com.example.learningkotlin.ui.screens.WorkoutDetailScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    workouts: MutableList<Workout>,  // <--- CHANGED: Now accepts Workouts
    onAddFunc: () -> Unit,
    onDeleteFunc: (Workout) -> Unit  // <--- CHANGED: Deletes a Workout object
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // --- SCREEN 1: HOME ---
        composable("home") {
            HomeScreen(
                workouts = workouts, // Pass the real list
                onAddClick = onAddFunc,
                onDeleteClick = onDeleteFunc,
                onItemClick = { workout ->
                    // Pass the workout name to the URL
                    navController.navigate("detail/${workout.name}")
                }
            )
        }

        // --- SCREEN 2: DETAIL ---
        composable(
            route = "detail/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutName = backStackEntry.arguments?.getString("name") ?: "Unknown"

            // Find the full workout object that matches this name
            // (In a real app, you'd find by ID, but Name works for now)
            val selectedWorkout = workouts.find { it.name == workoutName }

            if (selectedWorkout != null) {
                WorkoutDetailScreen(
                    workout = selectedWorkout,
                    onBackClick = { navController.popBackStack() },
                    onAddExerciseClick = { /* We will add this later */ }
                )
            } else {
                // Fallback if not found (optional safety)
                androidx.compose.material3.Text("Workout not found")
            }
        }
    }
}