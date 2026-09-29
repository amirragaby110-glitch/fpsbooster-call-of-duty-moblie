package com.a21optimizer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.a21optimizer.AppLog
import com.a21optimizer.data.AndroidDeviceStatusRepository
import com.a21optimizer.data.AndroidGameRepository
import com.a21optimizer.data.DeviceStatusRepository
import com.a21optimizer.data.GameRepository
import com.a21optimizer.data.UserPreferences
import com.a21optimizer.domain.BoostPlanner
import com.a21optimizer.domain.GameCatalog
import com.a21optimizer.domain.model.BoostPlan
import com.a21optimizer.domain.model.DeviceSnapshot
import com.a21optimizer.domain.model.GameInstallation
import com.a21optimizer.domain.model.PerformanceProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application,
    private val deviceRepository: DeviceStatusRepository = AndroidDeviceStatusRepository(application),
    private val gameRepository: GameRepository = AndroidGameRepository(application),
    private val preferences: UserPreferences = UserPreferences(application),
    private val boostPlanner: BoostPlanner = BoostPlanner(),
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(
        MainUiState(
            profile = preferences.profile(),
            selectedPackageName = GameCatalog.byPackageName(preferences.selectedPackage())
                ?.packageName
                ?: GameCatalog.default.packageName,
        ),
    )
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            deviceRepository.observeThermalStatus()
                .catch { throwable -> AppLog.error("Thermal listener unavailable", throwable) }
                .collect { level ->
                    _state.update { current ->
                        val updatedSnapshot = current.snapshot.copy(thermalLevel = level)
                        current.copy(
                            snapshot = updatedSnapshot,
                            readinessPlan = createPlan(
                                updatedSnapshot,
                                current.games,
                                current.selectedPackageName,
                                current.profile,
                            ),
                            boostPlan = current.boostPlan?.let {
                                createPlan(updatedSnapshot, current.games, current.selectedPackageName, current.profile)
                            },
                        )
                    }
                }
        }
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    deviceRepository.readSnapshot() to
                        gameRepository.installations(GameCatalog.supportedGames)
                }
            }
            result.onSuccess { (snapshot, games) ->
                _state.update { current ->
                    val selected = selectedPackage(current.selectedPackageName, games)
                    current.copy(
                        snapshot = snapshot,
                        games = games,
                        selectedPackageName = selected,
                        isRefreshing = false,
                        readinessPlan = createPlan(snapshot, games, selected, current.profile),
                        boostPlan = current.boostPlan?.let {
                            createPlan(snapshot, games, selected, current.profile)
                        },
                    )
                }
            }.onFailure { throwable ->
                AppLog.error("Device refresh failed", throwable)
                _state.update { it.copy(isRefreshing = false) }
                _effects.emit(UiEffect.ShowRefreshError)
            }
        }
    }

    fun selectProfile(profile: PerformanceProfile) {
        preferences.setProfile(profile)
        _state.update { current ->
            current.copy(
                profile = profile,
                readinessPlan = createPlan(
                    current.snapshot,
                    current.games,
                    current.selectedPackageName,
                    profile,
                ),
                boostPlan = current.boostPlan?.let {
                    createPlan(current.snapshot, current.games, current.selectedPackageName, profile)
                },
            )
        }
    }

    fun selectGame(packageName: String) {
        if (GameCatalog.byPackageName(packageName) == null) return
        preferences.setSelectedPackage(packageName)
        _state.update { current ->
            current.copy(
                selectedPackageName = packageName,
                readinessPlan = createPlan(current.snapshot, current.games, packageName, current.profile),
                boostPlan = null,
            )
        }
    }

    fun prepareBoost() {
        viewModelScope.launch {
            _state.update { it.copy(isPreparingBoost = true) }
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    deviceRepository.readSnapshot() to
                        gameRepository.installations(GameCatalog.supportedGames)
                }
            }
            result.onSuccess { (snapshot, games) ->
                _state.update { current ->
                    val selected = selectedPackage(current.selectedPackageName, games)
                    current.copy(
                        snapshot = snapshot,
                        games = games,
                        selectedPackageName = selected,
                        isPreparingBoost = false,
                        readinessPlan = createPlan(snapshot, games, selected, current.profile),
                        boostPlan = createPlan(snapshot, games, selected, current.profile),
                    )
                }
            }.onFailure { throwable ->
                AppLog.error("Pre-launch check failed", throwable)
                _state.update { it.copy(isPreparingBoost = false) }
                _effects.emit(UiEffect.ShowRefreshError)
            }
        }
    }

    fun dismissBoostPlan() {
        _state.update { it.copy(boostPlan = null) }
    }

    fun confirmLaunch() {
        val current = _state.value
        val plan = current.boostPlan ?: return
        if (!plan.launchAllowed) return
        val installation = current.games.firstOrNull {
            it.candidate.packageName == current.selectedPackageName && it.isInstalled
        } ?: return

        _state.update { it.copy(boostPlan = null) }
        _effects.tryEmit(UiEffect.LaunchGame(installation.candidate.packageName))
    }

    fun openStore() {
        _effects.tryEmit(UiEffect.OpenStore(_state.value.selectedPackageName))
    }

    fun openSystemSettings(destination: SettingsDestination) {
        _effects.tryEmit(UiEffect.OpenSettings(destination))
    }

    fun reportLaunchFailure() {
        _effects.tryEmit(UiEffect.ShowLaunchError)
        refresh()
    }

    private fun selectedPackage(
        requested: String,
        games: List<GameInstallation>,
    ): String {
        if (games.any { it.candidate.packageName == requested && it.isInstalled }) return requested
        return games.firstOrNull { it.isInstalled }?.candidate?.packageName
            ?: GameCatalog.byPackageName(requested)?.packageName
            ?: GameCatalog.default.packageName
    }

    private fun createPlan(
        snapshot: DeviceSnapshot,
        games: List<GameInstallation>,
        packageName: String,
        profile: PerformanceProfile,
    ): BoostPlan {
        val installation = games.firstOrNull { it.candidate.packageName == packageName }
            ?: GameInstallation(GameCatalog.default, isInstalled = false)
        return boostPlanner.createPlan(snapshot, installation, profile)
    }
}

data class MainUiState(
    val snapshot: DeviceSnapshot = DeviceSnapshot(),
    val games: List<GameInstallation> = GameCatalog.supportedGames.map {
        GameInstallation(candidate = it, isInstalled = false)
    },
    val selectedPackageName: String = GameCatalog.default.packageName,
    val profile: PerformanceProfile = PerformanceProfile.BALANCED,
    val isRefreshing: Boolean = true,
    val isPreparingBoost: Boolean = false,
    val readinessPlan: BoostPlan? = null,
    val boostPlan: BoostPlan? = null,
) {
    val selectedGame: GameInstallation?
        get() = games.firstOrNull { it.candidate.packageName == selectedPackageName }
}

sealed interface UiEffect {
    data class LaunchGame(val packageName: String) : UiEffect
    data class OpenStore(val packageName: String) : UiEffect
    data class OpenSettings(val destination: SettingsDestination) : UiEffect
    data object ShowRefreshError : UiEffect
    data object ShowLaunchError : UiEffect
}

enum class SettingsDestination {
    BATTERY_SAVER,
    DISPLAY,
    DO_NOT_DISTURB,
    APP_BATTERY,
}
