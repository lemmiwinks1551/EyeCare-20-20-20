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
import com.example.eyecare20_20_20.utils.Constants.INITIAL_DURATION_MS
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_ID
import com.example.eyecare20_20_20.utils.Constants.TIMER_STATE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationActions: NotificationActions

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val duration = MutableStateFlow(INITIAL_DURATION_MS)
    private lateinit var countDownTimer: CountDownTimer

    var minutes = mutableStateOf("20")
    var seconds = mutableStateOf("00")

    var currentState = mutableStateOf(TimerState.Idle)
    var progress: Float = 1f

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            duration.collect {
                // Пересчитываем минуты и секунды
                updateTimeUnits()

                // Вычисляем прогресс
                calculateProgress()

                // Обновляем UI в нотификации
                updateNotification()
            }
        }
    }

    override fun onBind(p0: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Вызов из Notification или UI
        when (intent?.getStringExtra(TIMER_STATE)) {
            TimerState.Started.name -> {
                startButtonCLick()
            }

            TimerState.Paused.name -> {
                pauseButtonClick()
            }

            TimerState.Reset.name -> {
                resetButtonClick()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        if (this::countDownTimer.isInitialized) {
            // Чтобы не было двух таймеров параллельно - отменяем другой
            countDownTimer.cancel()
        }

        if (currentState.value == TimerState.Timeout) {
            duration.value = INITIAL_DURATION_MS
        }

        currentState.value = TimerState.Started

        countDownTimer = object : CountDownTimer(
            duration.value,
            1000L
        ) {
            override fun onTick(p0: Long) {
                // Пересчитываем время (защита от отрицательного значения)
                duration.value = maxOf(0, p0)
            }

            override fun onFinish() {
                Log.i("countDownTimer", "${duration.value}")
                onTimerEnd()
            }
        }

        countDownTimer.start()
    }

    private fun onTimerEnd() {
        duration.value = 0

        playTimerEndSound()
        timeoutTimer()
        setStartButton()
    }

    private fun pauseTimer() {
        if (this::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        currentState.value = TimerState.Paused
    }

    private fun timeoutTimer() {
        countDownTimer.cancel()
        currentState.value = TimerState.Timeout
    }

    private fun updateTimeUnits() {
        // Обновляем минуты и секунды в удобный формат
        val totalSeconds = duration.value / 1000
        val m = totalSeconds / 60
        val s = totalSeconds % 60

        minutes.value = if (m < 10) "0$m" else m.toString()
        seconds.value = if (s < 10) "0$s" else s.toString()
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        /** Создает канал уведомлений c NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME */

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
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

    private fun calculateProgress() {
        // Вычислить прогресс в % для отрисовки
        progress = duration.value.toFloat() / INITIAL_DURATION_MS.toFloat()
    }

    // Set buttons in notification
    private fun setStopButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getPauseAction())
        notificationBuilder.addAction(notificationActions.getResetAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun setStartButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getStartAction())
        notificationBuilder.addAction(notificationActions.getResetAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun setResumeButton() {
        // Пересоздаем действия
        notificationBuilder.clearActions()
        notificationBuilder.addAction(notificationActions.getResumeAction())
        notificationBuilder.addAction(notificationActions.getResetAction())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    // Buttons clicks
    private fun startButtonCLick() {
        setStopButton()
        startForegroundService()
        startTimer()
        currentState.value = TimerState.Started
    }

    private fun pauseButtonClick() {
        pauseTimer()
        setResumeButton()
        currentState.value = TimerState.Paused
    }

    private fun resetButtonClick() {
        pauseTimer()
        setStartButton()
        duration.value = INITIAL_DURATION_MS
        currentState.value = TimerState.Reset
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

enum class TimerState {
    Idle,
    Started,
    Paused,
    Reset,
    Timeout
}