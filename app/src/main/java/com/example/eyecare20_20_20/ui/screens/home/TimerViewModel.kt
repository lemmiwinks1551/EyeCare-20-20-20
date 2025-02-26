package com.example.eyecare20_20_20.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private lateinit var job: Job
    companion object {
        const val TOTAL_TIME = 20 * 60 * 1000L // 20 минут
    }
    // TODO: почему то после нажатия на Сброс вычитает 1 секунду - 19:59

    // Состояние таймера, которое будет храниться в StateFlow

    // Приватный MutableStateFlow
    private val _state = MutableStateFlow(TimerState())

    // state — публичная версия, доступная для чтения
    val state = _state.asStateFlow()

    // Функция для старта/паузирования таймера
    fun startPauseTimer() {
        // Если таймер уже запущен, ставим его на паузу
        if (_state.value.isRunning) {
            _state.value = _state.value.copy(isRunning = false)
        } else {
            // Если таймер не запущен, запускаем его
            _state.value = _state.value.copy(isRunning = true)
            startTimer()
        }
    }

    // Функция для сброса таймера
    fun resetTimer() {
        // сбрасываем состояние до начальных значений
        job.cancel()
        _state.value = TimerState()
    }

    // Функция для старта таймера (уменьшаем время каждую секунду)
    private fun startTimer() {
        job = viewModelScope.launch { // используем viewModelScope для запуска корутины
            // Пока время больше 0 и таймер не на паузе, продолжаем отсчитывать
            while (_state.value.currentTime > 0 && _state.value.isRunning) {
                delay(1000L) // задержка на 1 секунду
                // Обновляем состояние: уменьшаем текущее время на 1 секунду и обновляем прогресс
                _state.value = _state.value.copy(
                    currentTime = _state.value.currentTime - 1000L, // уменьшаем время на 1 секунду
                    progress = (_state.value.currentTime - 1000L) / TOTAL_TIME.toFloat() // обновляем прогресс
                )
            }
        }
    }
}

// Data класс состояния таймера
data class TimerState(
    // начальное время таймера
    val currentTime: Long = TimerViewModel.TOTAL_TIME,

    // флаг, показывающий, запущен ли таймер
    val isRunning: Boolean = false,

    // прогресс от 0 до 1 (в зависимости от оставшегося времени)
    val progress: Float = 1f
)