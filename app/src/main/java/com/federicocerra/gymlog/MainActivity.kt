package com.federicocerra.gymlog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.federicocerra.gymlog.data.ThemePreferences
import com.federicocerra.gymlog.navigation.NavGraph
import com.federicocerra.gymlog.ui.theme.LearningKotlinTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private var navigateToWorkoutId by mutableStateOf<Int?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            ThemePreferences.load(this@MainActivity)
            if (ThemePreferences.showOverlayBubble.value) {
                checkOverlayPermission()
            }
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
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            try {
                startActivity(intent)
            } catch (_: Exception) {
                // Fallback to general settings if package specific fails
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            }
        }
    }
}
