package com.example.learningkotlin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningkotlin.ui.screens.HomeScreen
import com.example.learningkotlin.ui.screens.WorkoutDetailScreen
import com.example.learningkotlin.viewmodel.HomeViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    // 1. Initialize ViewModel here
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onWorkoutClick = { workout -> navController.navigate("detail/${workout.id}") }
            )
        }

        composable(
            route = "detail/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getInt("workoutId")
            val selectedWorkout = homeViewModel.workouts.find { it.id == workoutId }

            if (selectedWorkout != null) {
                WorkoutDetailScreen(
                    workout = selectedWorkout,
                    onStartWorkout = { homeViewModel.startWorkout(selectedWorkout) },
                    onFinishWorkout = { homeViewModel.finishWorkout(selectedWorkout) },
                    onBackClick = {
                        homeViewModel.onDetailScreenExit()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}