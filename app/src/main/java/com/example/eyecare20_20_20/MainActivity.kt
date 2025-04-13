package com.example.eyecare20_20_20

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eyecare20_20_20.domain.model.HomeMviState
import com.example.eyecare20_20_20.presentation.ui.navigation.Routes
import com.example.eyecare20_20_20.presentation.ui.screens.home.HomeScreen
import com.example.eyecare20_20_20.presentation.ui.screens.home.HomeScreenViewModel
import com.example.eyecare20_20_20.presentation.ui.screens.settings.SettingsScreen
import com.example.eyecare20_20_20.presentation.ui.theme.EyeCare202020Theme
import com.example.eyecare20_20_20.service.TimerService
import com.example.eyecare20_20_20.utils.getNavigationItems
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val homeScreenViewModel: HomeScreenViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            homeScreenViewModel.onServiceConnected(binder.getService()) // передаем сервис во ViewModel
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            homeScreenViewModel.onServiceDisconnected()
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val homeMviState by homeScreenViewModel.state.collectAsState()

            EyeCare202020Theme {
                if (homeMviState.isServiceBound) {
                    bottomNavigation(homeMviState)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }
}

@Composable
fun bottomNavigation(homeMviState: HomeMviState) {
    val navController = rememberNavController()
    val navItems = getNavigationItems()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(text = item.title) },
                        alwaysShowLabel = true,
                        icon = {
                            Icon(
                                imageVector = if (index == selectedItemIndex) {
                                    item.selectedIcon
                                } else {
                                    item.unselectedIcon
                                },
                                contentDescription = item.title
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.Home
            ) {
                composable(Routes.Home) { HomeScreen(state = homeMviState) }
                composable(Routes.Settings) { SettingsScreen() }
            }
        }
    }
}