package com.example.eyecare20_20_20.ui.screens.settings

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.example.eyecare20_20_20.model.SettingsMviState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

const val REQUEST_NOTIFICATION_PERMISSION = 1001

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    // Состояние UI
    private val _state = MutableStateFlow(SettingsMviState())
    val state: StateFlow<SettingsMviState> = _state.asStateFlow()

    fun allowNotificationCheckboxClick() {
    }

    fun checkNotificationPermission(context: Context) {
        _state.value = _state.value.copy(
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        )
    }
}