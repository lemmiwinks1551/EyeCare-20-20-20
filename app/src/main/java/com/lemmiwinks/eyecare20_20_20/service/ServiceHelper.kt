package com.lemmiwinks.eyecare20_20_20.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.lemmiwinks.eyecare20_20_20.MainActivity
import com.lemmiwinks.eyecare20_20_20.utils.Constants.CLICK_REQUEST_CODE
import com.lemmiwinks.eyecare20_20_20.utils.Constants.PAUSE_REQUEST_CODE
import com.lemmiwinks.eyecare20_20_20.utils.Constants.RESET_REQUEST_CODE
import com.lemmiwinks.eyecare20_20_20.utils.Constants.RESUME_REQUEST_CODE
import com.lemmiwinks.eyecare20_20_20.utils.Constants.TIMER_STATE

object ServiceHelper {
    /** Класс для управления таймером */
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

    /** Создает PendingIntent для остановки таймера (передает в сервис состояние "Paused").
     * */
    fun pausePendingIntent(context: Context): PendingIntent {
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
     * Создает PendingIntent для сброса таймера (передает в сервис состояние "Reset").
     */
    fun resetPendingIntent(context: Context): PendingIntent {
        val cancelIntent = Intent(context, TimerService::class.java).apply {
            putExtra(TIMER_STATE, TimerState.Reset.name)
        }
        return PendingIntent.getService(
            context, RESET_REQUEST_CODE, cancelIntent, flag
        )
    }
}