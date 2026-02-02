package com.example.learningkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.learningkotlin.data.ThemePreferences
import com.example.learningkotlin.navigation.NavGraph
import com.example.learningkotlin.ui.theme.LearningKotlinTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private var navigateToWorkoutId by mutableStateOf<Int?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            ThemePreferences.load(this@MainActivity)
        }
        checkAndRequestPermissions()
        
        intent?.getIntExtra("navigate_to_workout", -1)?.takeIf { it != -1 }?.let {
            navigateToWorkoutId = it
        }
        
        enableEdgeToEdge()
        setContent {
            LearningKotlinTheme(dynamicColor = false) {
                val navController = rememberNavController()
                
                NavGraph(
                    navController = navController,
                    startWorkoutId = navigateToWorkoutId,
                    onStartWorkoutHandled = { navigateToWorkoutId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getIntExtra("navigate_to_workout", -1).takeIf { it != -1 }?.let {
            navigateToWorkoutId = it
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}
