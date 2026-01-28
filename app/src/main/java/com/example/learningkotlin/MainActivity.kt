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
                
                // NavGraph now handles the ViewModel initialization internally
                NavGraph(navController = navController)
            }
        }
    }
}

//todo database!
//todo profiles, analysis.