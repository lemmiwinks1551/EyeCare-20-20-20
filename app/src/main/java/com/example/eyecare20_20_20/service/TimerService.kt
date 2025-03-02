package com.example.eyecare20_20_20.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.eyecare20_20_20.R

//  Unbound:
//  Foreground - видим для пользователя, показывая уведомление в статус баре,
//  когда активен, используется для музыки
//  Background - не виден пользователю, обращения к бекенду, работа с файлами приложения
//  После Oreo (8.0) - не работает, когда приложение не на переднем плане, только Foreground может

//  Bound:
//  предоставляет компоненту (Activity) возможность привязаться к сервису используя IPC, iBinder
//  прекращает работу, когда нет активных bind() подключений
//  (то есть, если приложение закроется, сервис тоже завершится)


class TimerService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        // если сервис не предоставляет интерфейс для других компоненов - возвращаем null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // триггерится когда другой компонент (Activity, например) пытается запустить сервис
        // здесь выполняется основная логика работы сервиса
        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.baseline_access_time_24)
            .setContentTitle("Run is active")
            .setContentText("Timer: 00:00")
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Вызывается при уничтожении сервиса
        // Очистка ресурсов: закрытие потоков, выключаем музыку и т.д.
    }

    enum class Actions {
        // Действия, которые может выполнять сервис
        START, STOP
    }
}