package com.example.eyecare20_20_20.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.eyecare20_20_20.R
import com.example.eyecare20_20_20.di.NotificationActions
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_CANCEL
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_PAUSE
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_START
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

//  Unbound:
//  Foreground - видим для пользователя, показывая уведомление в статус баре,
//  когда активен, используется для музыки
//  Background - не виден пользователю, обращения к бекенду, работа с файлами приложения
//  После Oreo (8.0) - не работает, когда приложение не на переднем плане, только Foreground может

//  Bound:
//  предоставляет компоненту (Activity) возможность привязаться к сервису используя IPC, iBinder
//  прекращает работу, когда нет активных bind() подключений
//  (то есть, если приложение закроется, сервис тоже завершится)

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
        when (intent?.getStringExtra(TIMER_STATE)) {
            TimerState.Started.name -> {
                setStopButton()
                startForegroundService()
                startTimer { minutes, seconds ->
                    updateNotification(minutes = minutes, seconds = seconds)
                }
            }

            TimerState.Stopped.name -> {
                pauseTimer()
                setResumeButton()
            }

            TimerState.Canceled.name -> {
                pauseTimer()
                cancelTimer()
                stopForegroundService()
            }
        }

        intent?.action.let {
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
                    pauseTimer()
                    cancelTimer()
                    stopForegroundService()
                    progress = 1f
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer(onTick: (m: String, s: String) -> Unit) {
        currentState.value = TimerState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.minus(1.seconds)

            calculateProgress()

            if (duration.inWholeSeconds == 0L) {
                // Если время вышло - останавливаем таймер
                playTimerEndSound()
                ServiceHelper.triggerForegroundService(
                    context = this@TimerService.applicationContext,
                    action = ACTION_SERVICE_CANCEL
                )
                notificationBuilder.clearActions()
                notificationBuilder.addAction(notificationActions.getStartAction())
                notificationBuilder.addAction(notificationActions.getCancelAction())
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
            updateTimeUnits()
            onTick(minutes.value, seconds.value)
        }
    }

    private fun pauseTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentState.value = TimerState.Stopped
    }

    private fun cancelTimer() {
        duration = INITIAL_DURATION_MINUTES.minutes
        currentState.value = TimerState.Idle
        updateTimeUnits()
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

    private fun setResumeButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getResumeAction())
        notificationBuilder.addAction(notificationActions.getCancelAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun playTimerEndSound() {
        // Воспроизведение звука
        val mediaPlayer = MediaPlayer.create(this, R.raw.timer_end_sound)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            it.release()
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
    Stopped,
    Canceled
}