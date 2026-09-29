package com.a21optimizer.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.a21optimizer.domain.BoostPlanner
import com.a21optimizer.domain.GameCatalog
import com.a21optimizer.domain.model.DeviceSnapshot
import com.a21optimizer.domain.model.GameInstallation
import com.a21optimizer.domain.model.PerformanceProfile
import com.a21optimizer.domain.model.ThermalLevel
import com.a21optimizer.ui.theme.A21OptimizerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class A21OptimizerAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun boostButtonIsReachableAndInvokesPreflight() {
        var invoked = false
        composeRule.setContent {
            A21OptimizerTheme {
                A21OptimizerApp(
                    state = healthyState(),
                    snackbarHostState = SnackbarHostState(),
                    onRefresh = {},
                    onSelectProfile = {},
                    onSelectGame = {},
                    onPrepareBoost = { invoked = true },
                    onDismissBoostPlan = {},
                    onConfirmLaunch = {},
                    onOpenStore = {},
                    onOpenSettings = {},
                )
            }
        }

        composeRule.onNodeWithTag("boost_button").performClick()
        composeRule.runOnIdle { assertTrue(invoked) }
    }

    @Test
    fun monitorStatesThatFpsIsUnavailable() {
        composeRule.setContent {
            A21OptimizerTheme {
                A21OptimizerApp(
                    state = healthyState(),
                    snackbarHostState = SnackbarHostState(),
                    onRefresh = {},
                    onSelectProfile = {},
                    onSelectGame = {},
                    onPrepareBoost = {},
                    onDismissBoostPlan = {},
                    onConfirmLaunch = {},
                    onOpenStore = {},
                    onOpenSettings = {},
                )
            }
        }

        composeRule.onNodeWithTag("tab_monitor").performClick()
        composeRule.onNodeWithText("FPS data unavailable through Android APIs")
            .assertIsDisplayed()
    }

    private fun healthyState(): MainUiState {
        val game = GameInstallation(GameCatalog.default, isInstalled = true)
        val snapshot = DeviceSnapshot(
            totalMemoryBytes = 4L * 1024 * 1024 * 1024,
            availableMemoryBytes = 2L * 1024 * 1024 * 1024,
            batteryLevelPercent = 80,
            batteryTemperatureCelsius = 31f,
            isCharging = false,
            isBatterySaverEnabled = false,
            thermalLevel = ThermalLevel.NONE,
            cpuCoreCount = 8,
            deviceName = "Samsung Galaxy A21",
            androidVersion = "Android 12",
        )
        return MainUiState(
            snapshot = snapshot,
            games = listOf(game),
            selectedPackageName = game.candidate.packageName,
            profile = PerformanceProfile.BALANCED,
            isRefreshing = false,
            readinessPlan = BoostPlanner().createPlan(
                snapshot,
                game,
                PerformanceProfile.BALANCED,
            ),
        )
    }
}
