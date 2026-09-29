package com.a21optimizer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a21optimizer.ui.A21OptimizerApp
import com.a21optimizer.ui.MainViewModel
import com.a21optimizer.ui.SettingsDestination
import com.a21optimizer.ui.UiEffect
import com.a21optimizer.ui.theme.A21OptimizerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A21OptimizerTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                val refreshError = stringResource(R.string.refresh_error)
                val launchError = stringResource(R.string.launch_error)
                val settingsError = stringResource(R.string.settings_error)

                LaunchedEffect(viewModel) {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is UiEffect.LaunchGame -> {
                                if (!launchGame(effect.packageName)) {
                                    viewModel.reportLaunchFailure()
                                }
                            }
                            is UiEffect.OpenStore -> openStore(effect.packageName)
                            is UiEffect.OpenSettings -> {
                                if (!openSettings(effect.destination)) {
                                    snackbarHostState.showSnackbar(settingsError)
                                }
                            }
                            UiEffect.ShowRefreshError -> snackbarHostState.showSnackbar(refreshError)
                            UiEffect.ShowLaunchError -> snackbarHostState.showSnackbar(launchError)
                        }
                    }
                }

                A21OptimizerApp(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onRefresh = viewModel::refresh,
                    onSelectProfile = viewModel::selectProfile,
                    onSelectGame = viewModel::selectGame,
                    onPrepareBoost = viewModel::prepareBoost,
                    onDismissBoostPlan = viewModel::dismissBoostPlan,
                    onConfirmLaunch = viewModel::confirmLaunch,
                    onOpenStore = viewModel::openStore,
                    onOpenSettings = viewModel::openSystemSettings,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun launchGame(packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        return runCatching {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
            // No overlay or monitor is kept alive. Removing this task releases the optimizer's UI,
            // ViewModel and thermal listener so the game gets the resources.
            finishAndRemoveTask()
        }.onFailure { AppLog.error("Unable to launch $packageName", it) }.isSuccess
    }

    private fun openStore(packageName: String) {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            "market://details?id=$packageName".toUri(),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        try {
            startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            runCatching {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=$packageName".toUri(),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT),
                )
            }.onFailure { AppLog.error("No store or browser available", it) }
        }
    }

    private fun openSettings(destination: SettingsDestination): Boolean {
        val action = when (destination) {
            SettingsDestination.BATTERY_SAVER -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            SettingsDestination.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
            SettingsDestination.DO_NOT_DISTURB -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            SettingsDestination.APP_BATTERY -> Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }
        return try {
            startActivity(Intent(action))
            true
        } catch (error: ActivityNotFoundException) {
            AppLog.error("Settings destination unavailable: $destination", error)
            false
        } catch (error: SecurityException) {
            AppLog.error("Settings destination blocked: $destination", error)
            false
        }
    }
}
