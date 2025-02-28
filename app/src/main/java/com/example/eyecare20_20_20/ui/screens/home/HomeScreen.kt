package com.example.eyecare20_20_20.ui.screens.home

import android.content.res.Configuration
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eyecare20_20_20.convertMsToString
import com.example.eyecare20_20_20.model.HomeMviAction
import com.example.eyecare20_20_20.model.HomeMviState
import com.example.eyecare20_20_20.ui.theme.Purple40
import com.example.eyecare20_20_20.ui.theme.PurpleGrey40

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
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
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun TimerScreen(
    state: HomeMviState,
    onAction: (HomeMviAction) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerDisplay(
            state = state,
            modifier = Modifier.size(200.dp)
        )
        TimerButtons(
            state = state,
            onAction = onAction
        )
    }
}

@Composable
fun TimerDisplay(state: HomeMviState, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        TimerProgress(state = state, modifier = modifier)
        Text(
            text = convertMsToString(state.currentTime),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimerProgress(state: HomeMviState, modifier: Modifier) {
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
            sweepAngle = 250f * state.progress,
            useCenter = false,
            size = Size(size, size),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TimerButtons(
    state: HomeMviState,
    onAction: (HomeMviAction) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = if (state.isRunning) {
                { onAction(HomeMviAction.PauseTimer) }
            } else {
                { onAction(HomeMviAction.StartTimer) }
            },
            enabled = !state.timeout
        ) {
            Text(
                if (state.isRunning) "Пауза"
                else "Старт"
            )
        }
        Button(
            onClick = { onAction(HomeMviAction.ResetTimer) }
        ) {
            Text("Сброс")
        }
    }
}
