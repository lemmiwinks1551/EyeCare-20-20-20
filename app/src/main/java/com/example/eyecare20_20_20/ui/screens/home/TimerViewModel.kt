package com.example.eyecare20_20_20.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eyecare20_20_20.model.HomeMviAction
import com.example.eyecare20_20_20.model.HomeMviState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private lateinit var job: Job

    companion object {
        // const val TOTAL_TIME = 20 * 60 * 1000L
        const val TOTAL_TIME = 3000L
    }

    // Состояние
    private val _state = MutableStateFlow(HomeMviState())
    val state: StateFlow<HomeMviState> = _state

    fun onAction(action: HomeMviAction) {
        when (action) {
            HomeMviAction.StartTimer -> startTimer()
            HomeMviAction.PauseTimer -> pauseTimer()
            HomeMviAction.ResetTimer -> resetTimer()
        }
    }

    private fun startTimer() {
        // Запускаем таймер, если он не запущен
        if (!state.value.isRunning) {
            _state.value = state.value.copy(isRunning = true) // Устанавилваем флаг

            // Запускаем корутину
            job = viewModelScope.launch {
                // Пока время больше 0 и таймер не на паузе, продолжаем отсчитывать время
                while (state.value.currentTime > 0 && state.value.isRunning) {
                    // Обновляем состояние: уменьшаем текущее время на 1 секунду и обновляем прогресс
                    delay(100L)
                    _state.value = state.value.copy(
                        currentTime = state.value.currentTime - 100L,
                        progress = (state.value.currentTime - 100L) / TOTAL_TIME.toFloat()
                    )

                    // Проверяем, вышло ли время
                    if (state.value.currentTime == 0L) {
                        // Если время вышло - меняем название на кнопке
                        _state.value = state.value.copy(
                            timeout = true
                        )
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        if (state.value.isRunning) {
            _state.value = state.value.copy(isRunning = false)
        }
    }

    private fun resetTimer() {
        // сбрасываем состояние до начальных значений и отменяем корутину
        job.cancel()
        _state.value = HomeMviState()
    }
}