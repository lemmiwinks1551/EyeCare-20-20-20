package com.example.eyecare20_20_20.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.eyecare20_20_20.R
import com.example.eyecare20_20_20.service.ServiceHelper
import com.example.eyecare20_20_20.utils.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    /** Предоставляет экземпляр NotificationCompat.Builder для создания уведомлений */

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Eye Care 20-20-20")
            .setContentText("00:00")
            .setSmallIcon(R.drawable.baseline_access_time_24)
            .setOngoing(true) // Уведомление нельзя убрать свайпом
            .addAction(
                0,
                "Стоп",
                ServiceHelper.stopPendingIntent(context)
            ) // Кнопка для остановки таймера
            .addAction(
                0,
                "Отмена",
                ServiceHelper.cancelPendingIntent(context)
            ) // Кнопка для сброса таймера
            .setContentIntent(ServiceHelper.clickPendingIntent(context)) // Открывает MainActivity при нажатии на уведомление
    }

    /** Предоставляет экземпляр NotificationManager для управления уведомлениями
     * NotificationManager — это системный сервис Android, который управляет уведомлениями.
     * Он позволяет создавать, обновлять и удалять уведомления в статус-баре.
     * */

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}

class NotificationActions @Inject constructor(@ApplicationContext private val context: Context) {
    fun getPauseAction() = NotificationCompat.Action(
        0,
        "Пауза",
        ServiceHelper.stopPendingIntent(context)
    )

    fun getResumeAction() = NotificationCompat.Action(
        0,
        "Продолжить",
        ServiceHelper.resumePendingIntent(context)
    )

    fun getCancelAction() = NotificationCompat.Action(
        0,
        "Отмена",
        ServiceHelper.stopPendingIntent(context)
    )
}
