package com.example.eyecare20_20_20.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.eyecare20_20_20.MainActivity
import com.example.eyecare20_20_20.utils.Constants.CANCEL_REQUEST_CODE
import com.example.eyecare20_20_20.utils.Constants.CLICK_REQUEST_CODE
import com.example.eyecare20_20_20.utils.Constants.RESUME_REQUEST_CODE
import com.example.eyecare20_20_20.utils.Constants.TIMER_STATE
import com.example.eyecare20_20_20.utils.Constants.PAUSE_REQUEST_CODE

object ServiceHelper {
    /** Класс для управления таймером */

    // Объект PendingIntent оборачивает функциональность объекта Intent, позволяя вашему приложению указать,
    // что другое приложение должно сделать от вашего имени  в ответ на будущее действие.

    private const val flag = PendingIntent.FLAG_IMMUTABLE

    /**
     * Создает PendingIntent для открытия MainActivity при нажатии на уведомление.
     * В Intent добавляется состояние таймера "Started".
     */
    fun clickPendingIntent(context: Context): PendingIntent {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(TIMER_STATE, TimerState.Started.name)
        }
        return PendingIntent.getActivity(
            context, CLICK_REQUEST_CODE, clickIntent, flag
        )
    }

    /** Создает PendingIntent для остановки таймера (передает в сервис состояние "Paused"). */
    fun stopPendingIntent(context: Context): PendingIntent {
        val stopIntent = Intent(context, TimerService::class.java).apply {
            putExtra(TIMER_STATE, TimerState.Paused.name)
        }
        return PendingIntent.getService(
            context, PAUSE_REQUEST_CODE, stopIntent, flag
        )
    }

    /**
     * Создает PendingIntent для возобновления таймера (передает в сервис состояние "Started").
     */
    fun resumePendingIntent(context: Context): PendingIntent {
        val resumeIntent = Intent(context, TimerService::class.java).apply {
            putExtra(TIMER_STATE, TimerState.Started.name)
        }
        return PendingIntent.getService(
            context, RESUME_REQUEST_CODE, resumeIntent, flag
        )
    }

    /**
     * Создает PendingIntent для отмены таймера (передает в сервис состояние "Canceled").
     */
    fun cancelPendingIntent(context: Context): PendingIntent {
        val cancelIntent = Intent(context, TimerService::class.java).apply {
            putExtra(TIMER_STATE, TimerState.Canceled.name)
        }
        return PendingIntent.getService(
            context, CANCEL_REQUEST_CODE, cancelIntent, flag
        )
    }

    /**
     * Запускает ForegroundService с заданным action
     * Используется для управления состоянием таймера через сервис
     */
    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, TimerService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}