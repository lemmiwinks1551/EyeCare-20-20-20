package com.example.eyecare20_20_20.ui.screens.home

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyecare20_20_20.service.ServiceHelper
import com.example.eyecare20_20_20.service.TimerService
import com.example.eyecare20_20_20.service.TimerState
import com.example.eyecare20_20_20.ui.theme.Purple40
import com.example.eyecare20_20_20.ui.theme.PurpleGrey40
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_CANCEL
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_START
import com.example.eyecare20_20_20.utils.Constants.ACTION_SERVICE_STOP

/* Layout-дерево
    HomeScreen
    │
    └── Box
    │
    └── TimerScreen
    │
    ├── Column
    │   │
    │   ├── TimerDisplay (Таймер)
    │   │   │
    │   │   ├── Box (Центрирует таймер)
    │   │   │   │
    │   │   │   ├── TimerProgress (Canvas - дуги)
    │   │   │   └── Text (Текущий таймер)
    │   │
    │   └── TimerButtons (Кнопки)
    │       │
    │       └── Row (Горизонтальное расположение кнопок)
    │           │
    │           ├── Button ("Старт"/"Пауза")
    │           └── Button ("Сброс")
*/

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreen(
    timerService: TimerService = TimerService()
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        TimerScreen(context, timerService)
    }
}

@Composable
fun TimerScreen(context: Context, timerService: TimerService) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerDisplay(
            modifier = Modifier.size(200.dp),
            timerService = timerService
        )
        TimerButtons(context, timerService)
    }
}

@Composable
fun TimerDisplay(modifier: Modifier, timerService: TimerService) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        TimerProgress(modifier = modifier)
        Text(
            text = "${timerService.minutes.value}:${timerService.seconds.value}",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimerProgress(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val strokeWidth = 10.dp.toPx()

        drawArc(
            color = PurpleGrey40,
            startAngle = -215f,
            sweepAngle = 250f,
            useCenter = false,
            size = Size(size, size),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = Purple40,
            startAngle = -215f,
            sweepAngle = 250f,
            // sweepAngle = 250f * state.progress,
            useCenter = false,
            size = Size(size, size),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TimerButtons(context: Context, timerService: TimerService) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = {
                ServiceHelper.triggerForegroundService(
                    context = context,
                    action = if (timerService.currentState.value == TimerState.Started) ACTION_SERVICE_STOP
                    else ACTION_SERVICE_START
                )
            }
        ) {
            Text(
                text = if (timerService.currentState.value == TimerState.Started) "Пауза"
                else if ((timerService.currentState.value == TimerState.Stopped)) "Продолжить"
                else "Старт"
            )
        }
        Button(
            onClick = {
                ServiceHelper.triggerForegroundService(
                    context = context, action = ACTION_SERVICE_CANCEL
                )
            }
        ) {
            Text("Сброс")
        }
    }
}
