package com.example.eyecare20_20_20.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.eyecare20_20_20.di.NotificationActions
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_CANCEL
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_PAUSE
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_START
import com.example.eyecare20_20_20.utils.Constants.INITIAL_DURATION_MS
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_ID
import com.example.eyecare20_20_20.utils.Constants.TIMER_STATE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class TimerService : Service() {
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationActions: NotificationActions

    private val binder = TimerBinder()
    private var duration = INITIAL_DURATION_MS
    private lateinit var countDownTimer: CountDownTimer

    var minutes = mutableStateOf("20")
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
                startTimer()
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
                    startTimer()
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
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        if (this::countDownTimer.isInitialized) {
            // Чтобы не было двух таймеров параллельно - отменяем другой
            countDownTimer.cancel()
        }

        currentState.value = TimerState.Started

        countDownTimer = object : CountDownTimer(
            INITIAL_DURATION_MS,
            1000L
        ) {
            override fun onTick(p0: Long) {
                // Пересчитываем время (защита от отрицательного значения)
                duration = maxOf(0, p0)

                // Вычисляем прогресс
                calculateProgress()

                // Обновляем минуты, секунды и прогресс в нотификации
                updateTimeUnits()

                // Обновляем UI в нотификации
                updateNotification()
            }

            override fun onFinish() {
                Log.i("countDownTimer", "$duration")
                onTimerEnd()
            }
        }

        countDownTimer.start()
    }

    private fun onTimerEnd() {
        progress = 0f

        playTimerEndSound()
        timeoutTimer()
        updateNotification()
        setStartButton()
    }

    private fun pauseTimer() {
        if (this::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        currentState.value = TimerState.Paused
    }

    private fun cancelTimer() {
        if (this::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        duration = INITIAL_DURATION_MS
        currentState.value = TimerState.Idle
        updateTimeUnits()
    }

    private fun timeoutTimer() {
        countDownTimer.cancel()
        duration = INITIAL_DURATION_MS
        currentState.value = TimerState.Timeout
    }

    private fun updateTimeUnits() {
        // Обновляем минуты и секунды в удобный формат
        val totalSeconds = duration / 1000
        val m = totalSeconds / 60
        val s = totalSeconds % 60

        minutes.value = if(m < 10) "0$m" else m.toString()
        seconds.value = if(s < 10) "0$s" else s.toString()
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

    private fun updateNotification() {
        /** Обновляет контент в уведомлении по id */

        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                "${minutes.value}:${seconds.value}",
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
        progress = duration.toFloat() / INITIAL_DURATION_MS.toFloat()
    }
}

enum class TimerState {
    Idle,
    Started,
    Paused,
    Timeout,
    Canceled
}