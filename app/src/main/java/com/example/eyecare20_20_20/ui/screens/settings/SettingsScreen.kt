package com.example.eyecare20_20_20.ui.screens.settings

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
@Preview(showBackground = true)
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Checkbox(
        checked = state.notificationsAllowed,
        onCheckedChange = {
            viewModel.allowNotificationCheckboxClick()
        })
}