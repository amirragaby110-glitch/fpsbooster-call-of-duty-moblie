package com.a21optimizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a21optimizer.R
import com.a21optimizer.domain.model.BoostPlan
import com.a21optimizer.domain.model.FindingCode
import com.a21optimizer.domain.model.FindingSeverity
import com.a21optimizer.domain.model.ReadinessFinding
import com.a21optimizer.ui.theme.DangerRed
import com.a21optimizer.ui.theme.SignalGreen
import com.a21optimizer.ui.theme.TacticalBlue
import com.a21optimizer.ui.theme.WarningAmber

@Composable
fun BoostPlanDialog(
    plan: BoostPlan,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onCheckAgain: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.prelaunch_title),
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.readiness_score, plan.readinessScore),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text(
                    text = if (plan.launchAllowed) stringResource(R.string.prelaunch_ready)
                    else stringResource(R.string.prelaunch_blocked),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                plan.findings.forEach { finding ->
                    FindingRow(finding)
                }
            }
        },
        confirmButton = {
            if (plan.launchAllowed) {
                Button(onClick = onLaunch) {
                    Text(stringResource(R.string.launch_game))
                }
            } else {
                Button(onClick = onCheckAgain) {
                    Text(stringResource(R.string.check_again))
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun FindingRow(finding: ReadinessFinding) {
    val color = findingColor(finding.severity)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = findingText(finding.code),
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun findingColor(severity: FindingSeverity): Color = when (severity) {
    FindingSeverity.PASS -> SignalGreen
    FindingSeverity.INFO -> TacticalBlue
    FindingSeverity.WARNING -> WarningAmber
    FindingSeverity.BLOCKING -> DangerRed
}

@Composable
private fun findingText(code: FindingCode): String = stringResource(
    when (code) {
        FindingCode.READY -> R.string.finding_ready
        FindingCode.GAME_NOT_INSTALLED -> R.string.finding_game_missing
        FindingCode.THERMAL_DANGER -> R.string.finding_thermal_danger
        FindingCode.THERMAL_WARM -> R.string.finding_thermal_warm
        FindingCode.BATTERY_TEMPERATURE_HIGH -> R.string.finding_battery_hot
        FindingCode.LOW_MEMORY -> R.string.finding_low_memory
        FindingCode.LOW_BATTERY -> R.string.finding_low_battery
        FindingCode.BATTERY_SAVER_ON -> R.string.finding_battery_saver
        FindingCode.LIMITED_SYSTEM_DATA -> R.string.finding_limited_data
    },
)
