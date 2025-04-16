package com.lemmiwinks.eyecare20_20_20.domain.model

import com.lemmiwinks.eyecare20_20_20.service.TimerService

data class HomeMviState(
    val isServiceBound: Boolean = false,
    val timerService: TimerService? = null
)
