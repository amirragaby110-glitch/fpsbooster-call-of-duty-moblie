package com.a21optimizer.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a21optimizer.R
import com.a21optimizer.domain.model.ThermalLevel
import com.a21optimizer.ui.MainUiState
import com.a21optimizer.ui.components.InformationCard
import com.a21optimizer.ui.components.MetricCard
import com.a21optimizer.ui.components.ScreenHeader
import com.a21optimizer.ui.components.SectionTitle
import com.a21optimizer.ui.theme.SignalGreen
import com.a21optimizer.ui.theme.TacticalBlue

@Composable
fun MonitorScreen(
    state: MainUiState,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    val snapshot = state.snapshot
    val memory = snapshot.availableMemoryBytes?.let { Formatter.formatFileSize(context, it) }
        ?: stringResource(R.string.unknown)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.monitor_eyebrow),
            title = stringResource(R.string.monitor_title),
            subtitle = stringResource(R.string.monitor_intro),
        )

        UnavailableMetricCard(
            label = stringResource(R.string.fps),
            body = stringResource(R.string.fps_unavailable),
        )
        UnavailableMetricCard(
            label = stringResource(R.string.frame_time),
            body = stringResource(R.string.frame_time_unavailable),
        )

        SectionTitle(stringResource(R.string.monitor_available_title))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                label = stringResource(R.string.memory),
                value = memory,
                detail = stringResource(R.string.available_format, memory),
                accent = TacticalBlue,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(R.string.battery),
                value = snapshot.batteryLevelPercent?.let { "$it%" }
                    ?: stringResource(R.string.unknown),
                detail = snapshot.batteryTemperatureCelsius?.let {
                    stringResource(R.string.temperature_format, it)
                } ?: stringResource(R.string.not_supported),
                accent = SignalGreen,
                modifier = Modifier.weight(1f),
            )
        }
        MetricCard(
            label = stringResource(R.string.thermal),
            value = thermalLabel(snapshot.thermalLevel),
            detail = thermalGuidance(snapshot.thermalLevel),
            accent = thermalColor(snapshot.thermalLevel),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.isRefreshing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.isRefreshing) stringResource(R.string.refreshing)
                else stringResource(R.string.refresh),
            )
        }

        InformationCard(
            title = stringResource(R.string.monitor_available_title),
            body = stringResource(R.string.monitor_available_body),
            accent = MaterialTheme.colorScheme.secondary,
        )
        InformationCard(
            title = stringResource(R.string.thermal_guidance_title),
            body = thermalGuidance(snapshot.thermalLevel),
            accent = thermalColor(snapshot.thermalLevel),
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun UnavailableMetricCard(label: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "—",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun thermalGuidance(level: ThermalLevel): String = when {
    level.isDangerous -> stringResource(R.string.thermal_guidance_hot)
    level == ThermalLevel.MODERATE -> stringResource(R.string.thermal_guidance_warm)
    else -> stringResource(R.string.thermal_guidance_normal)
}
