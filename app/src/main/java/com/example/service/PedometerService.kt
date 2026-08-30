package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.RouteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PedometerState(
    val dailySteps: Int = 0,
    val sessionSteps: Int = 0,
    val dailyGoalSteps: Int = 10000,
    val goalProgress: Float = 0.0f,
    val distanceKm: Double = 0.0,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val cadenceSpm: Int = 0, // Steps per minute
    val isTracking: Boolean = false,
    val sensorType: String = "Detecting sensor...",
    val hasHardwareSensor: Boolean = false,
    val lastStepTimestamp: Long = 0L,
    val hourlySteps: Map<Int, Int> = emptyMap() // Hour of day (0..23) -> step count
)

class PedometerService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var sensorManager: SensorManager
    private lateinit var prefs: SharedPreferences
    private var repository: RouteRepository? = null

    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var dailyStepBaseline: Float = -1f
    private var dailyOffsetSteps: Int = 0
    private var sessionStartSteps: Int = 0
    private var currentDailySteps: Int = 0
    private var lastRecordedDate: String = ""

    // Accelerometer fallback step detector variables
    private var lastAccelMagnitude = 0f
    private var lastStepTimeNs = 0L
    private val accelThreshold = 11.8f
    private val minStepIntervalNs = 250_000_000L // 250ms (~4 steps/sec max)

    // Cadence calculation
    private val recentStepTimestamps = mutableListOf<Long>()

    companion object {
        const val ACTION_START = "com.example.service.ACTION_START_PEDOMETER"
        const val ACTION_STOP = "com.example.service.ACTION_STOP_PEDOMETER"
        const val ACTION_RESET_DAILY = "com.example.service.ACTION_RESET_DAILY_STEPS"
        const val ACTION_ADD_STEPS = "com.example.service.ACTION_ADD_STEPS" // For calibration / demo
        const val EXTRA_STEP_COUNT = "extra_step_count"

        const val CHANNEL_ID = "campus_pedometer_channel"
        const val NOTIFICATION_ID = 5050

        private const val PREFS_NAME = "pedometer_preferences"
        private const val KEY_LAST_DATE = "key_pedometer_date"
        private const val KEY_BASELINE_STEPS = "key_baseline_steps"
        private const val KEY_ACCUMULATED_STEPS = "key_accumulated_steps"
        private const val KEY_GOAL_STEPS = "key_goal_steps"

        private val _pedometerState = MutableStateFlow(PedometerState())
        val pedometerState: StateFlow<PedometerState> = _pedometerState.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun resetDaily(context: Context) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_RESET_DAILY
            }
            context.startService(intent)
        }

        fun addManualSteps(context: Context, steps: Int) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_ADD_STEPS
                putExtra(EXTRA_STEP_COUNT, steps)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        repository = RouteRepository(applicationContext)

        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        createNotificationChannel()
        loadPersistedDailyState()
        startPeriodicStateUpdater()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startPedometerTracking()
            }
            ACTION_STOP -> {
                stopPedometerTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_RESET_DAILY -> {
                resetDailyCounters()
            }
            ACTION_ADD_STEPS -> {
                val added = intent.getIntExtra(EXTRA_STEP_COUNT, 0)
                if (added > 0) {
                    dailyOffsetSteps += added
                    currentDailySteps += added
                    saveDailyState()
                    updateCalculatedState(recordTimestamp = System.currentTimeMillis())
                }
            }
        }
        return START_STICKY
    }

    private fun loadPersistedDailyState() {
        val today = getTodayDateString()
        lastRecordedDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        val savedBaseline = prefs.getFloat(KEY_BASELINE_STEPS, -1f)
        val savedAccumulated = prefs.getInt(KEY_ACCUMULATED_STEPS, 0)

        if (lastRecordedDate != today) {
            // New day: reset daily counter
            lastRecordedDate = today
            dailyStepBaseline = -1f
            dailyOffsetSteps = 0
            currentDailySteps = 0
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putFloat(KEY_BASELINE_STEPS, -1f)
                .putInt(KEY_ACCUMULATED_STEPS, 0)
                .apply()
        } else {
            dailyStepBaseline = savedBaseline
            currentDailySteps = savedAccumulated
            dailyOffsetSteps = savedAccumulated
        }

        updateCalculatedState()
    }

    private fun saveDailyState() {
        prefs.edit()
            .putString(KEY_LAST_DATE, getTodayDateString())
            .putFloat(KEY_BASELINE_STEPS, dailyStepBaseline)
            .putInt(KEY_ACCUMULATED_STEPS, currentDailySteps)
            .apply()
    }

    private fun startPedometerTracking() {
        val notification = buildNotification("Campus Step Counter Active", "Tracking daily walking movement...")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var fgsType = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    fgsType = ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                }
                if (fgsType != 0) {
                    startForeground(NOTIFICATION_ID, notification, fgsType)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("PedometerService", "Error starting foreground service: ${e.message}", e)
            startForeground(NOTIFICATION_ID, notification)
        }

        registerSensorListeners()
        updateCalculatedState()
    }

    private fun registerSensorListeners() {
        var sensorRegistered = false
        var sensorName = "None"

        // 1. Primary: Hardware Step Counter (counts cumulative steps with lowest power consumption)
        if (stepCounterSensor != null) {
            val registered = sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (registered) {
                sensorRegistered = true
                sensorName = "Hardware Step Counter (${stepCounterSensor?.name ?: "Sensor"})"
                Log.d("PedometerService", "Registered TYPE_STEP_COUNTER sensor")
            }
        }

        // 2. Secondary: Step Detector (fires per step)
        if (stepDetectorSensor != null) {
            val registered = sensorManager.registerListener(
                this,
                stepDetectorSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            if (registered && !sensorRegistered) {
                sensorRegistered = true
                sensorName = "Hardware Step Detector"
                Log.d("PedometerService", "Registered TYPE_STEP_DETECTOR sensor")
            }
        }

        // 3. Fallback: Accelerometer (if hardware step sensors are unavailable e.g. emulators)
        if (!sensorRegistered && accelerometerSensor != null) {
            val registered = sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            if (registered) {
                sensorRegistered = true
                sensorName = "Motion Accelerometer (Software Step Algorithm)"
                Log.d("PedometerService", "Registered Accelerometer fallback for steps")
            }
        }

        _pedometerState.value = _pedometerState.value.copy(
            isTracking = true,
            hasHardwareSensor = (stepCounterSensor != null || stepDetectorSensor != null),
            sensorType = sensorName
        )
    }

    private fun stopPedometerTracking() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.e("PedometerService", "Error unregistering sensors: ${e.message}", e)
        }
        saveDailyState()
        _pedometerState.value = _pedometerState.value.copy(
            isTracking = false,
            cadenceSpm = 0
        )
    }

    private fun resetDailyCounters() {
        dailyStepBaseline = -1f
        dailyOffsetSteps = 0
        currentDailySteps = 0
        saveDailyState()
        updateCalculatedState()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val now = System.currentTimeMillis()
        val today = getTodayDateString()
        if (today != lastRecordedDate) {
            // Rollover at midnight
            loadPersistedDailyState()
        }

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val rawStepCount = event.values[0]
                if (rawStepCount <= 0f) return

                if (dailyStepBaseline < 0f || rawStepCount < dailyStepBaseline) {
                    // Initialize baseline or handle device reboot
                    dailyStepBaseline = rawStepCount
                    saveDailyState()
                }

                val hardwareStepsToday = (rawStepCount - dailyStepBaseline).roundToInt().coerceAtLeast(0)
                currentDailySteps = hardwareStepsToday + dailyOffsetSteps
                recordStepTimestamp(now)
                updateCalculatedState(now)
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    currentDailySteps++
                    recordStepTimestamp(now)
                    updateCalculatedState(now)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Accelerometer peak detection algorithm for devices without hardware step sensor
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                val delta = magnitude - lastAccelMagnitude
                lastAccelMagnitude = magnitude

                val nowNs = event.timestamp
                if (delta > 2.2f && magnitude > accelThreshold && (nowNs - lastStepTimeNs) > minStepIntervalNs) {
                    lastStepTimeNs = nowNs
                    currentDailySteps++
                    recordStepTimestamp(now)
                    updateCalculatedState(now)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("PedometerService", "Sensor accuracy changed: ${sensor?.name} -> $accuracy")
    }

    private fun recordStepTimestamp(now: Long) {
        synchronized(recentStepTimestamps) {
            recentStepTimestamps.add(now)
            // Keep timestamps from the last 30 seconds
            val cutoff = now - 30_000L
            recentStepTimestamps.removeAll { it < cutoff }
        }
    }

    private fun calculateCadence(now: Long): Int {
        synchronized(recentStepTimestamps) {
            val cutoff = now - 15_000L
            val recentCount = recentStepTimestamps.count { it >= cutoff }
            return if (recentCount > 1) {
                // Extrapolate 15-second window to 60 seconds (steps per minute)
                (recentCount * 4).coerceIn(0, 220)
            } else {
                0
            }
        }
    }

    private fun updateCalculatedState(recordTimestamp: Long = System.currentTimeMillis()) {
        val distanceKm = Math.round((currentDailySteps * 0.00076) * 100.0) / 100.0
        val calories = (currentDailySteps * 0.042).roundToInt()
        val activeMinutes = (currentDailySteps / 105).coerceAtLeast(0) // average ~105 steps/min active walk
        val goalSteps = 10000
        val progress = (currentDailySteps.toFloat() / goalSteps.toFloat()).coerceIn(0.0f, 1.0f)
        val cadence = calculateCadence(recordTimestamp)

        _pedometerState.value = _pedometerState.value.copy(
            dailySteps = currentDailySteps,
            dailyGoalSteps = goalSteps,
            goalProgress = progress,
            distanceKm = distanceKm,
            caloriesBurned = calories,
            activeMinutes = activeMinutes,
            cadenceSpm = cadence,
            lastStepTimestamp = recordTimestamp
        )

        updateNotification(
            title = "🚶 ${String.format("%,d", currentDailySteps)} Daily Campus Steps",
            content = "${distanceKm} km • $calories kcal • ${(progress * 100).toInt()}% of goal"
        )
    }

    private fun startPeriodicStateUpdater() {
        serviceScope.launch {
            while (isActive) {
                delay(4000L)
                if (_pedometerState.value.isTracking) {
                    val now = System.currentTimeMillis()
                    updateCalculatedState(now)
                    saveDailyState()
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Campus Step Counter & Pedometer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live daily step count, distance, and calories"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        try {
            val notification = buildNotification(title, content)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("PedometerService", "Failed to update notification: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopPedometerTracking()
        serviceScope.cancel()
        super.onDestroy()
    }
}
