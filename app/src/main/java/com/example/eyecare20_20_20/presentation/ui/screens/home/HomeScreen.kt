package com.example.eyecare20_20_20.presentation.ui.screens.home

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyecare20_20_20.domain.model.HomeMviState
import com.example.eyecare20_20_20.service.ServiceHelper
import com.example.eyecare20_20_20.service.TimerService
import com.example.eyecare20_20_20.service.TimerState
import com.example.eyecare20_20_20.presentation.ui.theme.Purple40
import com.example.eyecare20_20_20.presentation.ui.theme.PurpleGrey40

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

@Composable
@Preview(showBackground = true)
fun HomeScreen(
    state: HomeMviState = HomeMviState(
        isServiceBound = false,
        timerService = TimerService()
    )
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimerScreen(context, state)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppDescription()
        }
    }
}

@Composable
fun TimerScreen(context: Context, state: HomeMviState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerDisplay(
            modifier = Modifier.size(200.dp),
            state = state
        )
        TimerButtons(context, state)
    }
}

@Composable
fun TimerDisplay(modifier: Modifier, state: HomeMviState) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        TimerProgress(modifier = modifier, state = state)
        Text(
            text = "${state.timerService?.minutes?.value}:${state.timerService?.seconds?.value}",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimerProgress(modifier: Modifier, state: HomeMviState) {
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
        if (state.timerService?.minutes?.value != null) {
            drawArc(
                color = Purple40,
                startAngle = -215f,
                sweepAngle = 250f * (state.timerService.progress),
                useCenter = false,
                size = Size(size, size),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun TimerButtons(context: Context, state: HomeMviState) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = {
                /** В зависимости от состояния таймера отправляет соответствующее действие в ForegroundService. */
                when (state.timerService?.currentState?.value) {
                    TimerState.Started -> {
                        ServiceHelper.pausePendingIntent(context).send()
                    }

                    TimerState.Paused -> {
                        ServiceHelper.resumePendingIntent(context).send()
                    }

                    else -> {
                        ServiceHelper.resumePendingIntent(context).send()
                    }
                }
            }
        ) {
            Text(
                /** В зависимости от состояния таймера отправляет устанавливает текст кнопки */
                text =
                when(state.timerService?.currentState?.value) {
                    TimerState.Started -> {
                        "Пауза"
                    }
                    TimerState.Paused -> {
                        "Продолжить"
                    }
                    else -> {"Старт"}
                }
            )
        }
        Button(
            onClick = {
                ServiceHelper.resetPendingIntent(context).send()
            }
        ) {
            Text("Сброс")
        }
    }
}

@Composable
fun AppDescription() {
    Text(modifier = Modifier.padding(12.dp),
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        text = "Правило 20-20-20 помогает снизить напряжение глаз при работе за компьютером. " +
            "Каждые 20 минут делайте 20-секундный перерыв и смотрите на объект на расстоянии 6 метров.")
}
