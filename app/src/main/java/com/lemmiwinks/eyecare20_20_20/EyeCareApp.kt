package com.lemmiwinks.eyecare20_20_20

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EyeCareApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "running_channel",
            "Running notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}