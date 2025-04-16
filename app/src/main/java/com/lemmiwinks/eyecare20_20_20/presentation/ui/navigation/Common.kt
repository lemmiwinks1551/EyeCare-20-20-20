package com.lemmiwinks.eyecare20_20_20.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import com.lemmiwinks.eyecare20_20_20.domain.model.BottomNavItem

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
        )
    )
}
