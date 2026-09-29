package com.a21optimizer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a21optimizer.R
import com.a21optimizer.domain.model.PerformanceProfile
import com.a21optimizer.ui.components.BoostPlanDialog
import com.a21optimizer.ui.screens.DashboardScreen
import com.a21optimizer.ui.screens.MonitorScreen
import com.a21optimizer.ui.screens.SettingsScreen

@Composable
fun A21OptimizerApp(
    state: MainUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onSelectProfile: (PerformanceProfile) -> Unit,
    onSelectGame: (String) -> Unit,
    onPrepareBoost: () -> Unit,
    onDismissBoostPlan: () -> Unit,
    onConfirmLaunch: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: (SettingsDestination) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Text(
                                text = tab.marker,
                                fontWeight = FontWeight.Black,
                            )
                        },
                        label = { Text(stringResource(tab.labelResource)) },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                    )
                }
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp),
            ) {
                when (selectedTab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        state = state,
                        onRefresh = onRefresh,
                        onSelectProfile = onSelectProfile,
                        onSelectGame = onSelectGame,
                        onBoost = onPrepareBoost,
                        onOpenStore = onOpenStore,
                    )
                    AppTab.MONITOR -> MonitorScreen(
                        state = state,
                        onRefresh = onRefresh,
                    )
                    AppTab.SETTINGS -> SettingsScreen(onOpenSettings = onOpenSettings)
                }
            }
        }
    }

    state.boostPlan?.let { plan ->
        BoostPlanDialog(
            plan = plan,
            onDismiss = onDismissBoostPlan,
            onLaunch = onConfirmLaunch,
            onCheckAgain = {
                onDismissBoostPlan()
                onPrepareBoost()
            },
        )
    }
}

private enum class AppTab(
    val labelResource: Int,
    val marker: String,
) {
    DASHBOARD(R.string.nav_dashboard, "◆"),
    MONITOR(R.string.nav_monitor, "▥"),
    SETTINGS(R.string.nav_settings, "●"),
}
