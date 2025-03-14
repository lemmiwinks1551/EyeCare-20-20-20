package com.example.eyecare20_20_20.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyecare20_20_20.model.HomeMviAction
import com.example.eyecare20_20_20.model.HomeMviState
import com.example.eyecare20_20_20.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

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