package com.example.learningkotlin.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningkotlin.ui.screens.HomeScreen
import com.example.learningkotlin.ui.screens.WorkoutDetailScreen
import com.example.learningkotlin.viewmodel.HomeViewModel
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.BottomTimerBar

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    val homeViewModel: HomeViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onWorkoutClick = { workout -> navController.navigate("detail/${workout.id}") }
                )
            }

            composable(
                route = "detail/{workoutId}",
                arguments = listOf(navArgument("workoutId") { 
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getInt("workoutId") ?: -1
                val selectedWorkout = homeViewModel.workouts.find { it.id == workoutId }

                if (selectedWorkout != null) {
                    WorkoutDetailScreen(
                        workout = selectedWorkout,
                        viewModel = homeViewModel, // Passing ViewModel for timer control
                        isAnyOtherWorkoutActive = homeViewModel.isAnyOtherWorkoutActive(workoutId),
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

        // --- GLOBAL TIMER POPUP ---
        // This stays visible even when switching between Home and Detail
        if (homeViewModel.isRestTimerRunning) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp) // Extra padding to not cover bottom content
            ) {
                BottomTimerBar(
                    secondsRemaining = homeViewModel.restTimerSeconds,
                    onSkip = { homeViewModel.skipTimer() },
                    onAdd15 = { homeViewModel.add15Seconds() },
                    onSub15 = { homeViewModel.sub15Seconds() }
                )
            }
        }
    }
}