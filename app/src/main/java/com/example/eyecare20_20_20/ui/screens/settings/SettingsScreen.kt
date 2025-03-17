package com.example.eyecare20_20_20.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
@Preview(showBackground = true)
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize() // Заполняем весь экран
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth() // Заполняем всю ширину
                .padding(16.dp) // Отступы от краев экрана
        ) {
            Text(text = "Разрешить уведомления") // Текст слева

            Spacer(modifier = Modifier.weight(1f)) // Двигаем свич вправо

            Switch(
                checked = state.notificationsAllowed,
                onCheckedChange = { viewModel.allowNotificationCheckboxClick() }
            )
        }
    }
}
