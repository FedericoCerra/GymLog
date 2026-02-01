package com.example.learningkotlin.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.example.learningkotlin.ui.screens.*
import com.example.learningkotlin.viewmodel.HomeViewModel
import com.example.learningkotlin.viewmodel.AuthViewModel
import com.example.learningkotlin.ui.components.workoutDetailScreenHelpers.BottomTimerBar
import com.example.learningkotlin.service.WorkoutOverlayService

@Composable
fun NavGraph(
    navController: NavHostController,
    startWorkoutId: Int? = null,
    onStartWorkoutHandled: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    var selectedHistoryWorkout by remember { mutableStateOf<FinishedWorkout?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    
    // Auth-aware start destination
    val startDestination = if (authViewModel.currentUser == null) "auth" else "home"

    // Handle navigation from overlay bubble
    LaunchedEffect(startWorkoutId) {
        startWorkoutId?.let { id ->
            navController.navigate("detail/$id") {
                popUpTo("home") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onStartWorkoutHandled()
        }
    }

    // Handle Overlay Bubble Visibility based on navigation AND active workout status
    val activeWorkoutId = WorkoutOverlayService.activeWorkoutId.intValue
    LaunchedEffect(currentRoute, navBackStackEntry?.arguments, activeWorkoutId) {
        val workoutIdInRoute = navBackStackEntry?.arguments?.getInt("workoutId")
        val isViewingActiveWorkout = currentRoute?.startsWith("detail/") == true && 
                                   workoutIdInRoute == activeWorkoutId
        
        // Hide bubble if viewing active workout OR in settings screens
        val isSettingsScreen = currentRoute == "profile" || currentRoute == "app_settings" || 
                             currentRoute == "help_support" || currentRoute == "about_app"
        
        WorkoutOverlayService.setVisibility(!isViewingActiveWorkout && !isSettingsScreen)
    }

    // Handle Overlay Bubble Visibility based on app lifecycle (pause/resume)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentRoute, navBackStackEntry?.arguments, activeWorkoutId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Always show bubble when app is in background
                WorkoutOverlayService.setVisibility(true)
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // Re-evaluate based on current screen when returning to app
                val workoutIdInRoute = navBackStackEntry?.arguments?.getInt("workoutId")
                val isViewingActiveWorkout = currentRoute?.startsWith("detail/") == true && 
                                           workoutIdInRoute == activeWorkoutId
                val isSettingsScreen = currentRoute == "profile" || currentRoute == "app_settings" || 
                                     currentRoute == "help_support" || currentRoute == "about_app"
                
                WorkoutOverlayService.setVisibility(!isViewingActiveWorkout && !isSettingsScreen)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isRoutinesSelected = currentDestination?.hierarchy?.any { 
        it.route == "home" || it.route?.startsWith("detail/") == true 
    } == true
    val isHistorySelected = currentDestination?.hierarchy?.any { it.route == "history" } == true

    val showBottomBar = currentRoute in listOf("home", "history", "detail/{workoutId}")

    val barColor = Color(0xFF0F0F0F)
    val density = LocalDensity.current
    val systemBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val customBarHeight = 56.dp
    val totalBottomOffset = (if (showBottomBar) customBarHeight else 0.dp) + systemBottomPadding

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background) 
    ) {
        Scaffold(
            containerColor = Color.Transparent, 
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) 
            ) {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("auth") {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onWorkoutClick = { workout -> navController.navigate("detail/${workout.id}") },
                            onSummaryClick = {
                                navController.navigate("history") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onProfileClick = {
                                navController.navigate("profile")
                            },
                            bottomBarPadding = totalBottomOffset
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() },
                            onAppSettings = { navController.navigate("app_settings") },
                            onHelpSupport = { navController.navigate("help_support") },
                            onAboutApp = { navController.navigate("about_app") },
                            onLogout = {
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("app_settings") {
                        AppSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("help_support") {
                        HelpSupportScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("about_app") {
                        AboutAppScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("history") {
                        WorkoutHistoryScreen(
                            history = homeViewModel.history,
                            onWorkoutClick = { workout ->
                                selectedHistoryWorkout = workout
                                navController.navigate("history_recap")
                            },
                            onDeleteWorkout = { id -> homeViewModel.deleteFinishedWorkout(id) },
                            bottomBarPadding = totalBottomOffset
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
                                onDiscardWorkout = {
                                    homeViewModel.discardWorkout(selectedWorkout)
                                    navController.popBackStack("home", inclusive = false)
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
                                onClose = { navController.popBackStack("home", inclusive = false) },
                                onSave = { updated -> homeViewModel.updateFinishedWorkout(updated) }
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
                                },
                                onSave = { updated -> homeViewModel.updateFinishedWorkout(updated) }
                            )
                        }
                    }
                }

                // Floating UI Elements (Timer)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = totalBottomOffset)
                ) {
                    if (homeViewModel.isRestTimerRunning) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                        ) {
                            BottomTimerBar(
                                secondsRemaining = homeViewModel.restTimerSeconds,
                                totalSeconds = homeViewModel.initialRestTimerSeconds,
                                onSkip = { homeViewModel.skipTimer() },
                                onAdd15 = { homeViewModel.add15Seconds() },
                                onSub15 = { homeViewModel.sub15Seconds() }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(customBarHeight + systemBottomPadding)
                    .align(Alignment.BottomCenter)
                    .background(barColor) 
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(customBarHeight)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomBottomNavItem(
                        icon = Icons.Default.FitnessCenter,
                        label = "Routines",
                        selected = isRoutinesSelected,
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    CustomBottomNavItem(
                        icon = Icons.AutoMirrored.Filled.List,
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
