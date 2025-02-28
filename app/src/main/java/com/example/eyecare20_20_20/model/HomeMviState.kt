package com.example.eyecare20_20_20.model

import com.example.eyecare20_20_20.ui.screens.home.TimerViewModel

data class HomeMviState(
    // начальное время таймера
    val currentTime: Long = TimerViewModel.TOTAL_TIME,

    // флаг, показывающий, запущен ли таймер
    val isRunning: Boolean = false,

    // прогресс от 0 до 1 (в зависимости от оставшегося времени)
    val progress: Float = 1f,

    // время вышло
    val timeout: Boolean = false
)
