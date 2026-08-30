package com.example.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Helper to trigger rich haptic vibration patterns across Android devices
 * when users arrive at waypoints, milestones, or complete campus routes.
 */
object HapticFeedbackHelper {

    private const val TAG = "HapticFeedbackHelper"

    /**
     * Retrieves the system Vibrator service safely across API levels.
     */
    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get Vibrator service", e)
            null
        }
    }

    /**
     * Checks if the device has a physical vibrator motor.
     */
    fun hasVibrator(context: Context): Boolean {
        val vibrator = getVibrator(context) ?: return false
        return vibrator.hasVibrator()
    }

    /**
     * Distinct pleasant double-pulse haptic pattern when reaching a route waypoint.
     * Pattern: 60ms pulse -> 70ms pause -> 120ms accent pulse.
     */
    fun vibrateWaypointReached(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Timings: 0ms delay, 60ms first pulse, 70ms pause, 120ms second pulse
                val timings = longArrayOf(0, 60, 70, 120)
                // Amplitudes: 0, medium intensity (160/255), 0, high intensity (255/255)
                val amplitudes = intArrayOf(0, 160, 0, 255)
                
                if (vibrator.hasAmplitudeControl()) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    val effect = VibrationEffect.createWaveform(timings, -1)
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 60, 70, 120)
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating on waypoint arrival", e)
        }
    }

    /**
     * Triumphant 3-pulse vibration pattern when completing an entire walking route.
     */
    fun vibrateRouteCompleted(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 60, 80, 60, 200)
                val amplitudes = intArrayOf(0, 150, 0, 180, 0, 255)
                if (vibrator.hasAmplitudeControl()) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    val effect = VibrationEffect.createWaveform(timings, -1)
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 80, 60, 80, 60, 200)
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating on route completion", e)
        }
    }

    /**
     * Single crisp feedback tick.
     */
    fun vibrateTick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating tick", e)
        }
    }
}
