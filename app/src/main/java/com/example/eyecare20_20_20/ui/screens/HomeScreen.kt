package com.example.eyecare20_20_20.ui.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.eyecare20_20_20.ui.theme.GreenColor
import com.example.eyecare20_20_20.ui.theme.RedColor
import kotlinx.coroutines.delay

@Composable
@Preview(showBackground = true)
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        TimerScreen()
    }
}

@Composable
fun TimerScreen() {
    val totalTime by remember { mutableStateOf(1L * 2000L) } // 20 минут
    var currentTime by remember { mutableStateOf(totalTime) }
    var isTimeRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(1f) }

    LaunchedEffect(currentTime, isTimeRunning) {
        if (currentTime > 0 && isTimeRunning) {
            delay(1000L)
            currentTime -= 1000L
            progress = currentTime / totalTime.toFloat()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerDisplay(
            progress = progress,
            currentTime = currentTime,
            modifier = Modifier.size(200.dp)
        )
        TimerControls(
            isTimeRunning = isTimeRunning,
            currentTime,
            onStartPause = {
                if (currentTime <= 0L) {
                    currentTime = totalTime
                    isTimeRunning = true
                } else {
                    isTimeRunning = !isTimeRunning
                }
            }, onReset = {
                currentTime = totalTime
                isTimeRunning = false
                progress = 1f
            }
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

private fun convertMsToString(ms: Long): String {
    val minutes = ms / 60000L
    val seconds = ms % 60000L / 1000L
    val secondsStr = if (seconds.toString().length == 1) {
        "0${seconds}"
    } else {
        "$seconds"
    }
    return "$minutes:$secondsStr"
}