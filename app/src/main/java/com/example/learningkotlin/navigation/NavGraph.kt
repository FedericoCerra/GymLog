package com.example.learningkotlin.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.learningkotlin.ui.screens.WorkoutRecapScreen
import com.example.learningkotlin.viewmodel.HomeViewModel
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.BottomTimerBar

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    val homeViewModel: HomeViewModel = viewModel()

    // Added background color to the root Box to prevent white flashes
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
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
                        viewModel = homeViewModel,
                        isAnyOtherWorkoutActive = homeViewModel.isAnyOtherWorkoutActive(workoutId),
                        onStartWorkout = { homeViewModel.startWorkout(selectedWorkout) },
                        onFinishWorkout = { 
                            homeViewModel.finishWorkout(selectedWorkout)
                            navController.navigate("recap") {
                                launchSingleTop = true
                            }
                        },
                        onBackClick = {
                            homeViewModel.onDetailScreenExit()
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable("recap") {
                val lastWorkout = homeViewModel.lastFinishedWorkout
                if (lastWorkout != null) {
                    WorkoutRecapScreen(
                        finishedWorkout = lastWorkout,
                        onClose = {
                            // Use popBackStack to return smoothly to home
                            navController.popBackStack("home", inclusive = false)
                        }
                    )
                } else {
                    // Fallback only if we land here by mistake
                    LaunchedEffect(Unit) {
                        navController.popBackStack("home", inclusive = false)
                    }
                }
            }
        }

        if (homeViewModel.isRestTimerRunning) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
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