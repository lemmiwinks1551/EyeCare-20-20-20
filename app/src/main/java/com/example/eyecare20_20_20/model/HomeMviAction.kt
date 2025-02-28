package com.example.eyecare20_20_20.model

sealed interface HomeMviAction {
    // data object — это объект как data class, но без параметров
    data object StartTimer: HomeMviAction
    data object PauseTimer: HomeMviAction
    data object ResetTimer: HomeMviAction
}