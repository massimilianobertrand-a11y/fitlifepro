package it.fitlifepro.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import androidx.core.app.NotificationCompat
import it.fitlifepro.app.MainActivity

/**
 * Foreground service that drives the workout rest timer.
 * Holds a WakeLock so the timer keeps running when the screen turns off.
 * When the timer expires, it beeps + vibrates continuously until the user
 * taps STOP (via notification action or the button in the UI).
 */
class RestTimerService : Service() {

    // ── Public API (Intent actions / extras) ──────────────────────────────
    companion object {
        const val ACTION_START = "rest.START"
        const val ACTION_STOP  = "rest.STOP"
        const val EXTRA_SECONDS = "seconds"
        const val CH_REST = "fitlife_rest_timer"
        const val NOTIF_ID = 9001

        fun startIntent(ctx: Context, seconds: Int) =
            Intent(ctx, RestTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
            }

        fun stopIntent(ctx: Context) =
            Intent(ctx, RestTimerService::class.java).apply { action = ACTION_STOP }
    }

    // ── Internal state ────────────────────────────────────────────────────
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var secondsLeft = 0
    private var alarmRunnable: Runnable? = null
    private var toneGen: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var alarmActive = false

    // ── Lifecycle ─────────────────────────────────────────────────────────
    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        acquireWakeLock()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                stopAlarm()
                secondsLeft = intent.getIntExtra(EXTRA_SECONDS, 60)
                startForeground(NOTIF_ID, buildNotification(secondsLeft, false))
                scheduleCountdown()
            }
            ACTION_STOP -> {
                stopAlarm()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopAlarm()
        handler.removeCallbacksAndMessages(null)
        wakeLock?.release()
        wakeLock = null
        super.onDestroy()
    }

    // ── Countdown ─────────────────────────────────────────────────────────
    private fun scheduleCountdown() {
        handler.removeCallbacksAndMessages(null)
        val tick = object : Runnable {
            override fun run() {
                if (secondsLeft <= 0) {
                    startAlarm()
                    return
                }
                updateNotification(secondsLeft, false)
                secondsLeft--
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(tick)
    }

    // ── Alarm (repeating beep + vibration) ────────────────────────────────
    private fun startAlarm() {
        if (alarmActive) return
        alarmActive = true
        updateNotification(0, true)
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (_: Exception) {}

        alarmRunnable = object : Runnable {
            override fun run() {
                if (!alarmActive) return
                // Beep
                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
                } catch (_: Exception) {}
                // Vibrate
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 500), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 300, 200, 300, 200, 500), -1)
                }
                handler.postDelayed(this, 1500)
            }
        }
        handler.post(alarmRunnable!!)
    }

    private fun stopAlarm() {
        alarmActive = false
        alarmRunnable?.let { handler.removeCallbacks(it) }
        alarmRunnable = null
        vibrator?.cancel()
        toneGen?.stopTone()
        toneGen?.release()
        toneGen = null
    }

    // ── Notification ──────────────────────────────────────────────────────
    private fun buildNotification(secs: Int, ringing: Boolean): Notification {
        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, RestTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openPi = PendingIntent.getActivity(
            this, 1,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (ringing) "⏰ Recupero terminato!" else "⏱ Recupero in corso"
        val text  = if (ringing) "Tocca STOP per continuare l'allenamento"
                    else "${secs}s rimanenti"
        return NotificationCompat.Builder(this, CH_REST)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(openPi)
            .addAction(android.R.drawable.ic_media_next, "STOP", stopPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun updateNotification(secs: Int, ringing: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(secs, ringing))
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CH_REST, "Timer recupero", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Timer di recupero tra le serie" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FitLifePro::RestTimerWakeLock"
        ).apply { acquire(30 * 60 * 1000L) } // max 30 min
    }
}
