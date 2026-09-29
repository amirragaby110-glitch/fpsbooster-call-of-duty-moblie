package com.a21optimizer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a21optimizer.BuildConfig
import com.a21optimizer.R
import com.a21optimizer.ui.SettingsDestination
import com.a21optimizer.ui.components.InformationCard
import com.a21optimizer.ui.components.ScreenHeader
import com.a21optimizer.ui.components.SectionTitle
import com.a21optimizer.ui.theme.SignalGreen
import com.a21optimizer.ui.theme.TacticalBlue
import com.a21optimizer.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    onOpenSettings: (SettingsDestination) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.settings_eyebrow),
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
        )

        SectionTitle(stringResource(R.string.quick_system_settings))
        SettingsRow(
            title = stringResource(R.string.battery_saver_title),
            body = stringResource(R.string.battery_saver_body),
            accent = WarningAmber,
            onClick = { onOpenSettings(SettingsDestination.BATTERY_SAVER) },
        )
        SettingsRow(
            title = stringResource(R.string.display_title),
            body = stringResource(R.string.display_body),
            accent = TacticalBlue,
            onClick = { onOpenSettings(SettingsDestination.DISPLAY) },
        )
        SettingsRow(
            title = stringResource(R.string.dnd_title),
            body = stringResource(R.string.dnd_body),
            accent = SignalGreen,
            onClick = { onOpenSettings(SettingsDestination.DO_NOT_DISTURB) },
        )
        SettingsRow(
            title = stringResource(R.string.battery_optimization_title),
            body = stringResource(R.string.battery_optimization_body),
            accent = MaterialTheme.colorScheme.secondary,
            onClick = { onOpenSettings(SettingsDestination.APP_BATTERY) },
        )

        InformationCard(
            title = stringResource(R.string.privacy_title),
            body = stringResource(R.string.privacy_body),
            accent = SignalGreen,
        )
        InformationCard(
            title = stringResource(R.string.limits_title),
            body = stringResource(R.string.limits_body),
            accent = WarningAmber,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsRow(
    title: String,
    body: String,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(17.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "●",
                color = accent,
                style = MaterialTheme.typography.titleSmall,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.opens_android_settings),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
