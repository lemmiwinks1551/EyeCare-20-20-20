package com.example.eyecare20_20_20.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eyecare20_20_20.convertMsToString
import com.example.eyecare20_20_20.ui.theme.GreenColor
import com.example.eyecare20_20_20.ui.theme.RedColor

@Preview(showBackground = true)
@Composable
fun HomeScreen(viewModel: TimerViewModel = viewModel()) {
    // Функция collectAsState() преобразовывает StateFlow или LiveData в MutableState
    // При изменении в StateFlow, меняется value внутри MutableState и функция перевызывается
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        TimerScreen(
            currentTime = state.currentTime,
            progress = state.progress,
            isTimeRunning = state.isRunning,
            onStartPause = { viewModel.startPauseTimer() },
            onReset = { viewModel.resetTimer() }
        )
    }
}

@Composable
fun TimerScreen(
    currentTime: Long,
    progress: Float,
    isTimeRunning: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerDisplay(
            progress = progress,
            currentTime = currentTime,
            modifier = Modifier.size(200.dp)
        )
        TimerControls(
            isTimeRunning = isTimeRunning,
            currentTime = currentTime,
            onStartPause = onStartPause,
            onReset = onReset
        )
    }
}

@Composable
fun TimerDisplay(progress: Float, currentTime: Long, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        CircularProgress(progress = progress, modifier = modifier)
        Text(
            text = convertMsToString(currentTime),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CircularProgress(progress: Float, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val strokeWidth = 10.dp.toPx()

        drawArc(
            color = Color.DarkGray,
            startAngle = -215f,
            sweepAngle = 250f,
            useCenter = false,
            size = Size(size, size),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = GreenColor,
            startAngle = -215f,
            sweepAngle = 250f * progress,
            useCenter = false,
            size = Size(size, size),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TimerControls(
    isTimeRunning: Boolean,
    currentTime: Long,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    val timeout = currentTime == 0L

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onStartPause,
            colors = ButtonDefaults.buttonColors(
                containerColor =
                if (!isTimeRunning) GreenColor // Если таймер не включен
                else RedColor
            ),
            enabled = !timeout
        ) {
            Text(
                if (isTimeRunning) "Пауза"
                else "Старт"
            )
        }
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = RedColor
            )
        ) {
            Text("Сброс")
        }
    }
}
