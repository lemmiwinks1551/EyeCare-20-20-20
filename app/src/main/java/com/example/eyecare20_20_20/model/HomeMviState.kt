package com.example.eyecare20_20_20.model

import com.example.eyecare20_20_20.service.TimerService

data class HomeMviState(
    val isServiceBound: Boolean = false,
    val timerService: TimerService? = null
)
