package com.federicocerra.gymlog.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.provider.Settings
import android.view.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.federicocerra.gymlog.MainActivity
import com.federicocerra.gymlog.R
import com.federicocerra.gymlog.data.ThemePreferences
import com.federicocerra.gymlog.ui.theme.LearningKotlinTheme
import kotlinx.coroutines.*

class WorkoutOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastWorkoutName = "Workout"

    companion object {
        private const val CHANNEL_ID = "workout_overlay_channel"
        private const val NOTIFICATION_ID = 1001
        
        var isRunning = false
            private set

        val workoutStartTime = mutableLongStateOf(0L)
        val restTimerSeconds = mutableIntStateOf(0)
        val isTimerRunning = mutableStateOf(false)
        var activeWorkoutId = mutableIntStateOf(-1)
            private set
        private val isVisible = mutableStateOf(true)

        fun start(context: Context, workoutName: String, startTime: Long, workoutId: Int) {
            workoutStartTime.longValue = startTime
            activeWorkoutId.intValue = workoutId
            val intent = Intent(context, WorkoutOverlayService::class.java).apply {
                action = "START_WORKOUT"
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

        fun requestStartTimer(context: Context, seconds: Int) {
            val intent = Intent(context, WorkoutOverlayService::class.java).apply {
                action = "START_TIMER"
                putExtra("seconds", seconds)
            }
            context.startService(intent)
        }

        fun requestStopTimer(context: Context) {
            val intent = Intent(context, WorkoutOverlayService::class.java).apply {
                action = "STOP_TIMER"
            }
            context.startService(intent)
        }
        
        fun requestUpdateTimer(context: Context, delta: Int) {
            val intent = Intent(context, WorkoutOverlayService::class.java).apply {
                action = "UPDATE_TIMER"
                putExtra("delta", delta)
            }
            context.startService(intent)
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
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GymLog::WorkoutTimerWakeLock")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_WORKOUT" -> {
                handleStartWorkout(intent)
            }
            "START_TIMER" -> {
                val seconds = intent.getIntExtra("seconds", 0)
                startRestTimer(seconds)
            }
            "STOP_TIMER" -> {
                stopRestTimer()
            }
            "UPDATE_TIMER" -> {
                val delta = intent.getIntExtra("delta", 0)
                updateRestTimer(delta)
            }
        }
        return START_STICKY
    }

    private fun handleStartWorkout(intent: Intent) {
        val workoutName = intent.getStringExtra("workout_name") ?: "Workout"
        val startTime = intent.getLongExtra("start_time", workoutStartTime.longValue)
        val workoutId = intent.getIntExtra("workout_id", activeWorkoutId.intValue)
        
        lastWorkoutName = workoutName
        workoutStartTime.longValue = startTime
        activeWorkoutId.intValue = workoutId

        val notification = createNotification(workoutName)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        setupOverlay(startTime, workoutId)
    }

    private fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        restTimerSeconds.intValue = seconds
        isTimerRunning.value = true
        
        // Acquire WakeLock to keep CPU running
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(seconds * 1000L + 5000L) // Duration + buffer
        }
        
        timerJob = serviceScope.launch {
            while (restTimerSeconds.intValue > 0) {
                delay(1000L)
                restTimerSeconds.intValue -= 1
                updateNotificationWithTimer()
            }
            isTimerRunning.value = false
            if (wakeLock?.isHeld == true) { wakeLock?.release() }
            
            if (ThemePreferences.timerSound.value) {
                triggerTimerAlert()
            }
            updateNotificationWithTimer()
        }
    }

    private fun stopRestTimer() {
        timerJob?.cancel()
        restTimerSeconds.intValue = 0
        isTimerRunning.value = false
        if (wakeLock?.isHeld == true) { wakeLock?.release() }
        updateNotificationWithTimer()
    }

    private fun updateRestTimer(delta: Int) {
        val newVal = restTimerSeconds.intValue + delta
        if (newVal <= 0) {
            if (isTimerRunning.value && ThemePreferences.timerSound.value) {
                triggerTimerAlert()
            }
            stopRestTimer()
        } else {
            restTimerSeconds.intValue = newVal
            // Extend WakeLock if needed
            if (isTimerRunning.value && wakeLock?.isHeld == true) {
                wakeLock?.release()
                wakeLock?.acquire(newVal * 1000L + 5000L)
            }
            updateNotificationWithTimer()
        }
    }
    
    private fun updateNotificationWithTimer() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(lastWorkoutName))
    }

    private fun triggerTimerAlert() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.bell_notification)
            mediaPlayer.setVolume(0.4f, 0.4f)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mediaPlayer.setOnCompletionListener { it.release() }
            mediaPlayer.start()
        } catch (_: Exception) { }

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (_: Exception) { }
    }

    private fun setupOverlay(startTime: Long, workoutId: Int) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
        if (composeView != null || !hasPermission) return

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
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
                    val isVisibleState by isVisible
                    val showBubbleSetting by ThemePreferences.showOverlayBubble
                    
                    if (isVisibleState && showBubbleSetting) {
                        OverlayBubble(
                            startTime = startTime,
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

        try {
            windowManager.addView(composeView, params)
        } catch (_: Exception) { }
    }

    private fun createNotification(workoutName: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val showNotification = ThemePreferences.showWorkoutNotification.value
        
        val timerText = if (isTimerRunning.value && restTimerSeconds.intValue > 0) {
            val m = restTimerSeconds.intValue / 60
            val s = restTimerSeconds.intValue % 60
            " | Rest: %d:%02d".format(m, s)
        } else ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active Workout: $workoutName")
            .setContentText("Workout in progress$timerText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(showNotification)
            .setPriority(if (showNotification) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MIN)
            .setSilent(true) // Always silent updates to avoid spamming sounds
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
        timerJob?.cancel()
        serviceScope.cancel()
        if (wakeLock?.isHeld == true) { wakeLock?.release() }
        composeView?.let {
            windowManager.removeView(it)
        }
        composeView = null
    }
}

@Composable
fun OverlayBubble(
    startTime: Long,
    onMove: (Float, Float) -> Unit,
    onClick: () -> Unit
) {
    var workoutDurationSeconds by remember { mutableLongStateOf(0L) }
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Use the static states from the Service
    val timerSeconds = WorkoutOverlayService.restTimerSeconds.intValue
    val isTimerActive = WorkoutOverlayService.isTimerRunning.value

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
                .border(2.dp, primaryColor, RoundedCornerShape(24.dp))
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
                        tint = primaryColor,
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
                    tint = primaryColor,
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
