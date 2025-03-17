package com.example.eyecare20_20_20.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.eyecare20_20_20.model.SettingsMviState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    // Состояние UI
    private val _state = MutableStateFlow(SettingsMviState())
    val state: StateFlow<SettingsMviState> = _state.asStateFlow()

    fun allowNotificationCheckboxClick() {
        if (_state.value.notificationsAllowed) {
            _state.value = _state.value.copy(notificationsAllowed = false)
        } else {
            _state.value = _state.value.copy(notificationsAllowed = true)
        }
    }
}