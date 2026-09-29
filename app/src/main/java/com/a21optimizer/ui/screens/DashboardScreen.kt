package com.a21optimizer.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a21optimizer.R
import com.a21optimizer.domain.model.BoostPlan
import com.a21optimizer.domain.model.FindingSeverity
import com.a21optimizer.domain.model.GameInstallation
import com.a21optimizer.domain.model.GameRegion
import com.a21optimizer.domain.model.PerformanceProfile
import com.a21optimizer.domain.model.ThermalLevel
import com.a21optimizer.ui.MainUiState
import com.a21optimizer.ui.components.BoostButton
import com.a21optimizer.ui.components.InformationCard
import com.a21optimizer.ui.components.MetricCard
import com.a21optimizer.ui.components.ScreenHeader
import com.a21optimizer.ui.components.SectionTitle
import com.a21optimizer.ui.components.StatusPill
import com.a21optimizer.ui.theme.DangerRed
import com.a21optimizer.ui.theme.SignalGreen
import com.a21optimizer.ui.theme.TacticalBlue
import com.a21optimizer.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    state: MainUiState,
    onRefresh: () -> Unit,
    onSelectProfile: (PerformanceProfile) -> Unit,
    onSelectGame: (String) -> Unit,
    onBoost: () -> Unit,
    onOpenStore: () -> Unit,
) {
    val snapshot = state.snapshot
    val context = LocalContext.current
    val availableMemory = snapshot.availableMemoryBytes?.let { Formatter.formatFileSize(context, it) }
        ?: stringResource(R.string.unknown)
    val usedMemory = snapshot.usedMemoryBytes?.let { Formatter.formatFileSize(context, it) }
        ?: stringResource(R.string.unknown)
    val thermalColor = thermalColor(snapshot.thermalLevel)
    val selectedGame = state.selectedGame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.dashboard_eyebrow),
            title = stringResource(R.string.dashboard_title),
            subtitle = "${snapshot.deviceName} · ${snapshot.androidVersion}",
        )

        ReadinessCard(state.readinessPlan, state.isRefreshing)

        SectionTitle(
            title = stringResource(R.string.device_status),
            action = {
                TextButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                    Text(
                        if (state.isRefreshing) stringResource(R.string.refreshing)
                        else stringResource(R.string.refresh),
                    )
                }
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                label = stringResource(R.string.memory),
                value = availableMemory,
                detail = stringResource(R.string.used_format, usedMemory),
                accent = TacticalBlue,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(R.string.battery),
                value = snapshot.batteryLevelPercent?.let { "$it%" }
                    ?: stringResource(R.string.unknown),
                detail = batteryDetail(state),
                accent = if ((snapshot.batteryLevelPercent ?: 100) <= 15) WarningAmber else SignalGreen,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                label = stringResource(R.string.thermal),
                value = thermalLabel(snapshot.thermalLevel),
                detail = snapshot.batteryTemperatureCelsius?.let {
                    stringResource(R.string.temperature_format, it)
                } ?: stringResource(R.string.not_supported),
                accent = thermalColor,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(R.string.processor),
                value = snapshot.cpuCoreCount?.let { stringResource(R.string.cores_format, it) }
                    ?: stringResource(R.string.unknown),
                detail = stringResource(R.string.cpu_restricted),
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }

        SectionTitle(stringResource(R.string.performance_profile))
        ProfileSelector(
            selected = state.profile,
            onSelect = onSelectProfile,
        )
        InformationCard(
            title = profileName(state.profile),
            body = profileDescription(state.profile),
            accent = if (state.profile == PerformanceProfile.EXTREME) WarningAmber else SignalGreen,
        )
        if (state.profile == PerformanceProfile.EXTREME) {
            Text(
                text = stringResource(R.string.extreme_heat_warning),
                style = MaterialTheme.typography.bodySmall,
                color = WarningAmber,
            )
        }
        Text(
            text = stringResource(R.string.profile_truth_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SectionTitle(stringResource(R.string.game_launcher))
        GameSelector(
            games = state.games,
            selectedPackage = state.selectedPackageName,
            onSelect = onSelectGame,
        )
        SelectedGameCard(selectedGame)
        if (selectedGame?.isInstalled != true) {
            OutlinedButton(
                onClick = onOpenStore,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.open_official_store))
            }
        }

        Spacer(Modifier.height(2.dp))
        BoostButton(
            label = if (state.isPreparingBoost) {
                stringResource(R.string.preparing_boost)
            } else {
                stringResource(R.string.boost_game)
            },
            enabled = !state.isPreparingBoost,
            loading = state.isPreparingBoost,
            onClick = onBoost,
            modifier = Modifier.testTag("boost_button"),
        )
        Text(
            text = stringResource(R.string.boost_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ReadinessCard(plan: BoostPlan?, isRefreshing: Boolean) {
    val blocking = plan?.findings?.any { it.severity == FindingSeverity.BLOCKING } == true
    val warning = plan?.findings?.any { it.severity == FindingSeverity.WARNING } == true
    val color = when {
        blocking -> DangerRed
        warning -> WarningAmber
        else -> SignalGreen
    }
    val title = when {
        blocking -> stringResource(R.string.status_blocked)
        warning -> stringResource(R.string.status_attention)
        else -> stringResource(R.string.status_ready)
    }
    val score = plan?.readinessScore ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.09f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = if (isRefreshing) stringResource(R.string.refreshing)
                        else stringResource(R.string.readiness_score, score),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusPill(
                    text = when {
                        blocking -> stringResource(R.string.readiness_hold_short)
                        warning -> stringResource(R.string.readiness_check_short)
                        else -> stringResource(R.string.readiness_ready_short)
                    },
                    color = color,
                )
            }
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileSelector(
    selected: PerformanceProfile,
    onSelect: (PerformanceProfile) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PerformanceProfile.entries.forEach { profile ->
            FilterChip(
                selected = profile == selected,
                onClick = { onSelect(profile) },
                label = { Text(profileName(profile)) },
                modifier = Modifier.testTag("profile_${profile.storageKey}"),
            )
        }
    }
}

@Composable
private fun GameSelector(
    games: List<GameInstallation>,
    selectedPackage: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        games.forEach { game ->
            FilterChip(
                selected = game.candidate.packageName == selectedPackage,
                onClick = { onSelect(game.candidate.packageName) },
                label = { Text(regionName(game.candidate.region)) },
                leadingIcon = if (game.isInstalled) {
                    { Text("•", color = SignalGreen, fontWeight = FontWeight.Black) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun SelectedGameCard(game: GameInstallation?) {
    val installed = game?.isInstalled == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = game?.appLabel ?: stringResource(R.string.call_of_duty_mobile),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = game?.candidate?.packageName.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(10.dp))
            StatusPill(
                text = if (installed) stringResource(R.string.installed)
                else stringResource(R.string.game_not_installed),
                color = if (installed) SignalGreen else WarningAmber,
            )
        }
    }
}

@Composable
private fun batteryDetail(state: MainUiState): String {
    val snapshot = state.snapshot
    val status = if (snapshot.isCharging == true) {
        stringResource(R.string.charging)
    } else {
        stringResource(R.string.not_charging)
    }
    val temperature = snapshot.batteryTemperatureCelsius?.let {
        stringResource(R.string.temperature_format, it)
    }
    return listOfNotNull(status, temperature).joinToString(" · ")
}

@Composable
fun thermalLabel(level: ThermalLevel): String = stringResource(
    when (level) {
        ThermalLevel.NONE -> R.string.thermal_normal
        ThermalLevel.LIGHT -> R.string.thermal_light
        ThermalLevel.MODERATE -> R.string.thermal_moderate
        ThermalLevel.SEVERE -> R.string.thermal_severe
        ThermalLevel.CRITICAL -> R.string.thermal_critical
        ThermalLevel.EMERGENCY -> R.string.thermal_emergency
        ThermalLevel.SHUTDOWN -> R.string.thermal_shutdown
        ThermalLevel.UNSUPPORTED -> R.string.not_supported
    },
)

fun thermalColor(level: ThermalLevel) = when (level) {
    ThermalLevel.SEVERE,
    ThermalLevel.CRITICAL,
    ThermalLevel.EMERGENCY,
    ThermalLevel.SHUTDOWN,
    -> DangerRed
    ThermalLevel.MODERATE -> WarningAmber
    ThermalLevel.NONE,
    ThermalLevel.LIGHT,
    -> SignalGreen
    ThermalLevel.UNSUPPORTED -> TacticalBlue
}

@Composable
fun profileName(profile: PerformanceProfile): String = when (profile) {
    PerformanceProfile.BALANCED -> stringResource(R.string.profile_balanced)
    PerformanceProfile.PERFORMANCE -> stringResource(R.string.profile_performance)
    PerformanceProfile.EXTREME -> stringResource(R.string.profile_extreme)
}

@Composable
private fun profileDescription(profile: PerformanceProfile): String = when (profile) {
    PerformanceProfile.BALANCED -> stringResource(R.string.profile_balanced_description)
    PerformanceProfile.PERFORMANCE -> stringResource(R.string.profile_performance_description)
    PerformanceProfile.EXTREME -> stringResource(R.string.profile_extreme_description)
}

@Composable
fun regionName(region: GameRegion): String = when (region) {
    GameRegion.GLOBAL -> stringResource(R.string.region_global)
    GameRegion.GARENA -> stringResource(R.string.region_garena)
    GameRegion.VIETNAM -> stringResource(R.string.region_vietnam)
    GameRegion.CHINA -> stringResource(R.string.region_china)
}
