package com.example.learningkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.learningkotlin.navigation.NavGraph
import com.example.learningkotlin.ui.theme.LearningKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearningKotlinTheme(dynamicColor = false) {
                val navController = rememberNavController()

                // State and logic are now managed in ViewModels
                NavGraph(navController = navController)
            }
        }
    }
}

//todo use viewModels, so that data isnt messy anymore (kinda big)
//todo implement start and finish of workout
//todo make so that user selects exercises
//todo database!
//todo profiles, recap, analysis.