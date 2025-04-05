package com.example.eyecare20_20_20.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.eyecare20_20_20.di.NotificationActions
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_CANCEL
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_PAUSE
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_START
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_TIMEOUT
import com.example.eyecare20_20_20.utils.Constants.INITIAL_DURATION_MINUTES
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_ID
import com.example.eyecare20_20_20.utils.Constants.TIMER_STATE
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class TimerService : Service() {
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationActions: NotificationActions

    private val binder = TimerBinder()
    private var duration: Duration = INITIAL_DURATION_MINUTES.minutes
    private lateinit var timer: Timer

    var minutes = mutableStateOf(duration.inWholeMinutes.toString())
    var seconds = mutableStateOf("00")
    var currentState = mutableStateOf(TimerState.Idle)
    var progress: Float = 1f

    override fun onBind(p0: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Вызов из Notification
        when (intent?.getStringExtra(TIMER_STATE)) {
            TimerState.Started.name -> {
                setStopButton()
                startForegroundService()
                startTimer { minutes, seconds ->
                    updateNotification(minutes = minutes, seconds = seconds)
                }
            }

            TimerState.Paused.name -> {
                pauseTimer()
                setResumeButton()
            }

            TimerState.Canceled.name -> {
                cancelTimer()
                stopForegroundService()
                progress = 1f
            }
        }

        intent?.action.let {
            // Вызов из UI
            when (it) {
                ACTION_SERVICE_START -> {
                    setStopButton()
                    startForegroundService()
                    startTimer { minutes, seconds ->
                        updateNotification(minutes = minutes, seconds = seconds)
                    }
                }

                ACTION_SERVICE_PAUSE -> {
                    pauseTimer()
                    setResumeButton()
                }

                ACTION_SERVICE_CANCEL -> {
                    cancelTimer()
                    stopForegroundService()
                    progress = 1f
                }

                ACTION_SERVICE_TIMEOUT -> {
                    playTimerEndSound()
                    pauseTimer()
                    timeoutTimer()
                    setStartButton()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer(onTick: (m: String, s: String) -> Unit) {
        if (this::timer.isInitialized) {
            timer.cancel()
        }

        currentState.value = TimerState.Started

        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.minus(1.seconds)

            updateTimeUnits()
            calculateProgress()
            onTick(minutes.value, seconds.value)

            if (duration.inWholeSeconds == 0L) {
                ServiceHelper.triggerForegroundService(
                    context = this@TimerService.applicationContext,
                    action = ACTION_SERVICE_TIMEOUT
                )
            }
        }
    }

    private fun pauseTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentState.value = TimerState.Paused
    }

    private fun cancelTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        duration = INITIAL_DURATION_MINUTES.minutes
        currentState.value = TimerState.Idle
        updateTimeUnits()
    }

    private fun timeoutTimer() {
        duration = INITIAL_DURATION_MINUTES.minutes
        currentState.value = TimerState.Timeout
    }

    private fun updateTimeUnits() {
        duration.toComponents { minutes, seconds, _ ->
            this@TimerService.minutes.value = minutes.toString()
            this@TimerService.seconds.value = if (seconds < 10) "0$seconds" else seconds.toString()
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopForegroundService() {
        cancelTimer()
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        /** Создает канал уведомлений c NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME */

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(minutes: String, seconds: String) {
        /** Обновляет контент в уведомление по id */
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                "$minutes:$seconds",
            )
                .setProgress(100, (progress * 100).toInt(), false)
                .build()
        )
    }

    private fun setStopButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getPauseAction())
        notificationBuilder.addAction(notificationActions.getCancelAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun setStartButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getStartAction())
        notificationBuilder.addAction(notificationActions.getCancelAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun setResumeButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getResumeAction())
        notificationBuilder.addAction(notificationActions.getCancelAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun playTimerEndSound() {
        // Воспроизведение звука
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private fun calculateProgress() {
        // Вычислить прогресс в % для отрисовки
        progress = duration.inWholeSeconds.toFloat() / (INITIAL_DURATION_MINUTES.toFloat() * 60f)
    }
}

enum class TimerState {
    Idle,
    Started,
    Paused,
    Timeout,
    Canceled
}