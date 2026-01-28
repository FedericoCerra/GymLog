package com.example.learningkotlin.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningkotlin.model.FinishedWorkout
import com.example.learningkotlin.ui.screens.HomeScreen
import com.example.learningkotlin.ui.screens.WorkoutDetailScreen
import com.example.learningkotlin.ui.screens.WorkoutHistoryScreen
import com.example.learningkotlin.ui.screens.WorkoutRecapScreen
import com.example.learningkotlin.viewmodel.HomeViewModel
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.BottomTimerBar

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    val homeViewModel: HomeViewModel = viewModel()
    var selectedHistoryWorkout by remember { mutableStateOf<FinishedWorkout?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "history", "detail/{workoutId}")

    val density = LocalDensity.current
    val systemNavBarHeight = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val customBarHeight = 56.dp
    // totalBottomOffset is the distance from screen bottom to top of our custom nav bar
    val totalBottomOffset = if (showBottomBar) customBarHeight + systemNavBarHeight else 0.dp

    // Root background matches theme to avoid "white box" gaps
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background) 
    ) {
        Scaffold(
            containerColor = Color.Transparent, 
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Handle status bar safe area
            ) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onWorkoutClick = { workout -> navController.navigate("detail/${workout.id}") },
                            bottomBarPadding = totalBottomOffset
                        )
                    }

                    composable("history") {
                        WorkoutHistoryScreen(
                            history = homeViewModel.history,
                            onWorkoutClick = { workout ->
                                selectedHistoryWorkout = workout
                                navController.navigate("history_recap")
                            }
                        )
                    }

                    composable(
                        route = "detail/{workoutId}",
                        arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
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
                                    navController.navigate("recap") { launchSingleTop = true }
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
                                onClose = { navController.popBackStack("home", inclusive = false) }
                            )
                        } else {
                            LaunchedEffect(Unit) { navController.popBackStack("home", inclusive = false) }
                        }
                    }

                    composable("history_recap") {
                        selectedHistoryWorkout?.let { workout ->
                            WorkoutRecapScreen(
                                finishedWorkout = workout,
                                onClose = {
                                    navController.popBackStack()
                                    selectedHistoryWorkout = null
                                }
                            )
                        }
                    }
                }

                if (homeViewModel.isRestTimerRunning) {
                    Box(modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = totalBottomOffset + 8.dp) 
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

        // Custom Navigation Bar
        if (showBottomBar) {
            Surface(
                color = Color(0xFF0F0F0F), // Dark Gray/Black color from Hevy
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(customBarHeight),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isHomeSelected = currentRoute == "home" || currentRoute?.startsWith("detail/") == true
                        CustomBottomNavItem(
                            icon = Icons.Default.FitnessCenter,
                            label = "Routines",
                            selected = isHomeSelected,
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        val isHistorySelected = currentRoute == "history"
                        CustomBottomNavItem(
                            icon = Icons.Default.List,
                            label = "History",
                            selected = isHistorySelected,
                            onClick = {
                                navController.navigate("history") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    // This Spacer forces the color to extend behind the system buttons
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

@Composable
private fun CustomBottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, 
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
