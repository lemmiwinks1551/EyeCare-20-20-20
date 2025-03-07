package com.example.eyecare20_20_20.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_CANCEL
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_START
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_PAUSE
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_ID
import com.example.eyecare20_20_20.utils.Constants.TIMER_STATE
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
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

    private val binder = TimerBinder()

    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    var minutes = mutableStateOf("00")
    var seconds = mutableStateOf("00")
    var currentState = mutableStateOf(TimerState.Idle)

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
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer(onTick: (m: String, s: String) -> Unit) {
        currentState.value = TimerState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
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
        duration = Duration.ZERO
        currentState.value = TimerState.Idle
        updateTimeUnits()
    }

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@TimerService.minutes.value = minutes.toString()
            this@TimerService.seconds.value = seconds.toString()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(minutes: String, seconds: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                "$minutes:$seconds"
            ).build()
        )
    }

    @SuppressLint("RestrictedApi")
    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Stop",
                ServiceHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun setResumeButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Продолжить",
                ServiceHelper.resumePendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
}


enum class TimerState {
    Idle,
    Started,
    Stopped,
    Canceled
}