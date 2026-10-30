package com.cartoniq.pulse.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cartoniq.pulse.data.PreferencesManager
import com.cartoniq.pulse.sensor.SensorManagerHelper
import com.cartoniq.pulse.ui.screens.DashboardScreen
import com.cartoniq.pulse.ui.screens.DiagnosticsScreen
import com.cartoniq.pulse.ui.screens.LightMeterScreen
import com.cartoniq.pulse.ui.screens.MetalDetectorScreen
import com.cartoniq.pulse.ui.screens.SettingsScreen
import com.cartoniq.pulse.ui.screens.VibrationScreen

sealed class Screen {
    data object Dashboard : Screen()
    data object MetalDetector : Screen()
    data object VibrationMeter : Screen()
    data object LightMeter : Screen()
    data object Settings : Screen()
    data object Diagnostics : Screen()
}

class NavigationController(initial: Screen = Screen.Dashboard) {
    private val backStack = mutableStateListOf<Screen>(initial)

    val currentScreen: Screen
        get() = backStack.lastOrNull() ?: Screen.Dashboard

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(screen)
        }
    }

    fun popBackStack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            true
        } else {
            false
        }
    }
}

@Composable
fun PulseNavGraph(
    sensorHelper: SensorManagerHelper,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val navController = remember { NavigationController(Screen.Dashboard) }

    // Intercept system back button
    BackHandler(enabled = navController.currentScreen != Screen.Dashboard) {
        navController.popBackStack()
    }

    Crossfade(
        targetState = navController.currentScreen,
        animationSpec = tween(250),
        label = "ScreenTransition",
        modifier = modifier
    ) { screen ->
        when (screen) {
            is Screen.Dashboard -> DashboardScreen(
                sensorHelper = sensorHelper,
                onOpenMetal = { navController.navigateTo(Screen.MetalDetector) },
                onOpenVibration = { navController.navigateTo(Screen.VibrationMeter) },
                onOpenLight = { navController.navigateTo(Screen.LightMeter) },
                onOpenSettings = { navController.navigateTo(Screen.Settings) },
                onOpenDiagnostics = { navController.navigateTo(Screen.Diagnostics) }
            )

            is Screen.MetalDetector -> MetalDetectorScreen(
                sensorHelper = sensorHelper,
                preferencesManager = preferencesManager,
                onNavigateBack = { navController.popBackStack() },
                onOpenDiagnostics = { navController.navigateTo(Screen.Diagnostics) }
            )

            is Screen.VibrationMeter -> VibrationScreen(
                sensorHelper = sensorHelper,
                preferencesManager = preferencesManager,
                onNavigateBack = { navController.popBackStack() },
                onOpenDiagnostics = { navController.navigateTo(Screen.Diagnostics) }
            )

            is Screen.LightMeter -> LightMeterScreen(
                sensorHelper = sensorHelper,
                preferencesManager = preferencesManager,
                onNavigateBack = { navController.popBackStack() },
                onOpenDiagnostics = { navController.navigateTo(Screen.Diagnostics) }
            )

            is Screen.Settings -> SettingsScreen(
                preferencesManager = preferencesManager,
                onNavigateBack = { navController.popBackStack() }
            )

            is Screen.Diagnostics -> DiagnosticsScreen(
                sensorHelper = sensorHelper,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
