package com.example.learningkotlin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel // Import for viewModel()
import com.example.learningkotlin.ui.screens.HomeScreen
import com.example.learningkotlin.ui.screens.WorkoutDetailScreen
import com.example.learningkotlin.viewmodel.HomeViewModel // Import the new ViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    // Instantiate HomeViewModel, which will manage the workout data
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                workouts = homeViewModel.workouts,
                onAddClick = homeViewModel::addWorkout,
                onDeleteClick = homeViewModel::deleteWorkout,
                onWorkoutClick = { workout -> navController.navigate("detail/${workout.id}") },
                onRenameClick = homeViewModel::renameWorkout
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
            // Access workouts from the ViewModel
            val selectedWorkout = homeViewModel.workouts.find { it.id == workoutId }

            // 3. Show Screen
            if (selectedWorkout != null) {
                WorkoutDetailScreen(
                    workout = selectedWorkout,
                    onBackClick = {
                        homeViewModel.onDetailScreenExit() // <--- Call save function on ViewModel
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}