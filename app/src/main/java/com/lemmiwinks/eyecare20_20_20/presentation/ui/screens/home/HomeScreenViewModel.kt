package com.lemmiwinks.eyecare20_20_20.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import com.lemmiwinks.eyecare20_20_20.domain.model.HomeMviState
import com.lemmiwinks.eyecare20_20_20.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor() : ViewModel() {

    // Состояние UI
    private val _state = MutableStateFlow(HomeMviState())
    val state: StateFlow<HomeMviState> = _state.asStateFlow()

    // Устанавливаем сервис при подключении
    fun onServiceConnected(service: TimerService) {
        _state.value = _state.value.copy(isServiceBound = true, timerService = service)
    }

    // Обновляем состояние при отключении
    fun onServiceDisconnected() {
        _state.value = _state.value.copy(isServiceBound = false, timerService = null)
    }
}