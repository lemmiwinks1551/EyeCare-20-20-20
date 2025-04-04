package com.example.eyecare20_20_20.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.eyecare20_20_20.model.SettingsMviState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    // Состояние UI
    private val _state = MutableStateFlow(SettingsMviState())
    val state: StateFlow<SettingsMviState> = _state.asStateFlow()

    // Вызывается из UI, чтобы обновить разрешение
    fun updateNotificationPermission(isGranted: Boolean) {
        _state.update { it.copy(notificationsEnabled = isGranted) }
    }

    // Вызывается из UI при старте, чтобы проверить, есть ли уже разрешение
    fun checkNotificationPermission(context: Context) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        updateNotificationPermission(granted)
    }
}