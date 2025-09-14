package com.sufo.lexinote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.sufo.lexinote.ui.navigation.BottomNavItem
import com.sufo.lexinote.ui.navigation.NavigationCommand
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.data.preferences.SettingsDataStore
import com.sufo.lexinote.data.preferences.UserPreferences


import com.sufo.lexinote.ui.theme.LexiNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sufo.lexinote.ui.navigation.AppNavHost

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationService: NavigationService

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val userPreferences by settingsDataStore.userPreferencesFlow.collectAsState(
                initial = UserPreferences(themeMode = "SYSTEM", notificationsEnabled = true)
            )
            val useDarkTheme = when (userPreferences.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            LexiNoteTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                LaunchedEffect("navigation") {
                    navigationService.command.onEach { command ->
                        when (command) {
                            is NavigationCommand.NavigateTo -> {
                                navController.navigate(command.route)
                            }
                            is NavigationCommand.PopBackStack -> {
                                navController.popBackStack()
                            }
                            is NavigationCommand.NavigateToAndClearBackStack -> {
                                navController.navigate(command.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                    }.launchIn(this)
                }

                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Review,
                    BottomNavItem.Explore,
                    BottomNavItem.Me
                )

                val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                modifier = Modifier
                                    .height(60.dp) // 1. Reduced height
                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))),
                                containerColor = MaterialTheme.colorScheme.background // 2. Background color
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            // 3. Navigation logic is already correct
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp) // 1. Reduced spacing
                                            ) {
                                                Icon(item.icon, contentDescription = null)
                                                Text(text = stringResource(id = item.titleResId), style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors( // 4. Selected colors
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = MaterialTheme.colorScheme.background
                                        )
                                    )
                                }
                            }
                        }
                    }
                ){ innerPadding->
                    AppNavHost(
                        navController = navController,
                        navigationService = navigationService,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
