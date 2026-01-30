package com.example.learningkotlin.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.learningkotlin.MainActivity
import com.example.learningkotlin.ui.theme.HevyBlue
import com.example.learningkotlin.ui.theme.LearningKotlinTheme
import kotlinx.coroutines.delay

class WorkoutOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null

    companion object {
        private const val CHANNEL_ID = "workout_overlay_channel"
        private const val NOTIFICATION_ID = 1001
        
        var isRunning = false
            private set

        // Shared states for the overlay
        private var workoutStartTime = mutableLongStateOf(0L)
        private var restTimerSeconds = mutableIntStateOf(0)
        private var isTimerRunning = mutableStateOf(false)
        var activeWorkoutId = mutableIntStateOf(-1)
            private set
        private var isVisible = mutableStateOf(true)

        fun start(context: Context, workoutName: String, startTime: Long, workoutId: Int) {
            workoutStartTime.longValue = startTime
            activeWorkoutId.intValue = workoutId
            val intent = Intent(context, WorkoutOverlayService::class.java).apply {
                putExtra("workout_name", workoutName)
                putExtra("start_time", startTime)
                putExtra("workout_id", workoutId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateTimer(seconds: Int, isRunning: Boolean) {
            restTimerSeconds.intValue = seconds
            isTimerRunning.value = isRunning
        }

        fun setVisibility(visible: Boolean) {
            isVisible.value = visible
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkoutOverlayService::class.java))
            activeWorkoutId.intValue = -1
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val workoutName = intent?.getStringExtra("workout_name") ?: "Workout"
        val startTime = intent?.getLongExtra("start_time", workoutStartTime.longValue) ?: workoutStartTime.longValue
        val workoutId = intent?.getIntExtra("workout_id", activeWorkoutId.intValue) ?: activeWorkoutId.intValue
        activeWorkoutId.intValue = workoutId

        val notification = createNotification(workoutName)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (composeView == null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this))) {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 200
            }

            val lifecycleOwner = object : LifecycleOwner, SavedStateRegistryOwner {
                private val lifecycleRegistry = LifecycleRegistry(this)
                private val savedStateRegistryController = SavedStateRegistryController.create(this)

                override val lifecycle: Lifecycle get() = lifecycleRegistry
                override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

                fun handleEvent(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)
                fun performRestore(state: android.os.Bundle?) = savedStateRegistryController.performRestore(state)
            }
            lifecycleOwner.performRestore(null)
            lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)

            composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                
                setContent {
                    LearningKotlinTheme(dynamicColor = false) {
                        AnimatedVisibility(
                            visible = isVisible.value,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            OverlayBubble(
                                startTime = startTime,
                                timerSeconds = restTimerSeconds.intValue,
                                isTimerActive = isTimerRunning.value,
                                onMove = { dx, dy ->
                                    params?.let {
                                        it.x += dx.toInt()
                                        it.y += dy.toInt()
                                        windowManager.updateViewLayout(this@apply, it)
                                    }
                                },
                                onClick = {
                                    val launchIntent = Intent(this@apply.context, MainActivity::class.java).apply {
                                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        putExtra("navigate_to_workout", workoutId)
                                    }
                                    startActivity(launchIntent)
                                }
                            )
                        }
                    }
                }
            }

            windowManager.addView(composeView, params)
        }

        return START_NOT_STICKY
    }

    private fun createNotification(workoutName: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active Workout")
            .setContentText("Workout: $workoutName is in progress")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        composeView?.let {
            windowManager.removeView(it)
        }
        composeView = null
    }
}

@Composable
fun OverlayBubble(
    startTime: Long,
    timerSeconds: Int,
    isTimerActive: Boolean,
    onMove: (Float, Float) -> Unit,
    onClick: () -> Unit
) {
    var workoutDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(startTime) {
        while (true) {
            val now = System.currentTimeMillis()
            workoutDurationSeconds = (now - startTime) / 1000
            delay(1000L)
        }
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }
    
    fun formatTimer(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    Surface(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x, dragAmount.y)
                }
            }
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
                .border(2.dp, HevyBlue, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTimerActive && timerSeconds > 0) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = HevyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formatTimer(timerSeconds),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .size(1.dp, 16.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
                
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = HevyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = formatDuration(workoutDurationSeconds),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
