package com.salahkeeper.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.ui.components.AnimatedMeshGradient
import com.salahkeeper.app.ui.components.GlassNavBar
import com.salahkeeper.app.ui.components.NavDestination
import com.salahkeeper.app.ui.screens.HistoryScreen
import com.salahkeeper.app.ui.screens.HomeScreen
import com.salahkeeper.app.ui.screens.OemBatteryScreen
import com.salahkeeper.app.ui.screens.OnboardingScreen
import com.salahkeeper.app.ui.screens.QazaScreen
import com.salahkeeper.app.ui.screens.SettingsScreen
import com.salahkeeper.app.ui.theme.SalahKeeperTheme
import com.salahkeeper.app.ui.viewmodel.HistoryViewModel
import com.salahkeeper.app.ui.viewmodel.HomeViewModel
import com.salahkeeper.app.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

enum class AppScreen {
    MAIN_NAV,
    OEM_GUIDE,
    ONBOARDING
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val settings by settingsViewModel.settingsFlow.collectAsState(initial = settingsViewModel.settings.value)
            val todayOverview by homeViewModel.todayOverview.collectAsState()

            SalahKeeperTheme(
                darkTheme = settings.isDarkMode,
                reduceEffects = settings.reduceEffects
            ) {
                val hazeState = remember { HazeState() }
                var currentScreen by remember(settings.isOnboardingCompleted) {
                    mutableStateOf(if (settings.isOnboardingCompleted) AppScreen.MAIN_NAV else AppScreen.ONBOARDING)
                }
                var currentNavDestination by remember { mutableStateOf(NavDestination.HOME) }

                val activePrayer = todayOverview?.nextPrayer?.type ?: PrayerType.FAJR

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(hazeState)
                ) {
                    // Mesh gradient background
                    AnimatedMeshGradient(activePrayer = activePrayer)

                    when (currentScreen) {
                        AppScreen.ONBOARDING -> {
                            OnboardingScreen(
                                onComplete = {
                                    // mark onboarding completed
                                    settingsViewModel.toggleGps(true)
                                    currentScreen = AppScreen.MAIN_NAV
                                },
                                hazeState = hazeState
                            )
                        }
                        AppScreen.OEM_GUIDE -> {
                            OemBatteryScreen(
                                onBack = { currentScreen = AppScreen.MAIN_NAV },
                                hazeState = hazeState
                            )
                        }
                        AppScreen.MAIN_NAV -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AnimatedContent(
                                    targetState = currentNavDestination,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "NavTransition"
                                ) { target ->
                                    when (target) {
                                        NavDestination.HOME -> {
                                            HomeScreen(
                                                homeViewModel = homeViewModel,
                                                settingsViewModel = settingsViewModel,
                                                hazeState = hazeState
                                            )
                                        }
                                        NavDestination.HISTORY -> {
                                            HistoryScreen(
                                                historyViewModel = historyViewModel,
                                                hazeState = hazeState
                                            )
                                        }
                                        NavDestination.QAZA -> {
                                            QazaScreen(
                                                historyViewModel = historyViewModel,
                                                hazeState = hazeState
                                            )
                                        }
                                        NavDestination.SETTINGS -> {
                                            SettingsScreen(
                                                settingsViewModel = settingsViewModel,
                                                onNavigateToOemGuide = { currentScreen = AppScreen.OEM_GUIDE },
                                                hazeState = hazeState
                                            )
                                        }
                                    }
                                }

                                // Floating Glass Nav Bar
                                GlassNavBar(
                                    currentDestination = currentNavDestination,
                                    onDestinationSelected = { currentNavDestination = it },
                                    hazeState = hazeState,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
