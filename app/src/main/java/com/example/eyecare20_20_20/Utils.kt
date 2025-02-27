package com.example.eyecare20_20_20

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import com.example.eyecare20_20_20.model.BottomNavItem
import com.example.eyecare20_20_20.ui.navigation.Routes


fun getNavigationItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            title = "Главная",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Routes.Home
        ),
        BottomNavItem(
            title = "Настройки",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = Routes.Settings
        ),
    )
}

fun convertMsToString(ms: Long): String {
    val minutes = ms / 60000L
    val seconds = ms % 60000L / 1000L
    val secondsStr = if (seconds.toString().length == 1) {
        "0${seconds}"
    } else {
        "$seconds"
    }
    return "$minutes:$secondsStr"
}
