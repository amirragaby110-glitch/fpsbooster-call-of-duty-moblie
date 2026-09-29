package com.a21optimizer.domain

import com.a21optimizer.domain.model.BoostPlan
import com.a21optimizer.domain.model.DeviceSnapshot
import com.a21optimizer.domain.model.FindingCode
import com.a21optimizer.domain.model.FindingSeverity
import com.a21optimizer.domain.model.GameInstallation
import com.a21optimizer.domain.model.PerformanceProfile
import com.a21optimizer.domain.model.ReadinessFinding
import com.a21optimizer.domain.model.ThermalLevel

class BoostPlanner {
    fun createPlan(
        snapshot: DeviceSnapshot,
        game: GameInstallation,
        profile: PerformanceProfile,
    ): BoostPlan {
        val findings = mutableListOf<ReadinessFinding>()
        var score = 100
        var launchAllowed = game.isInstalled

        if (!game.isInstalled) {
            findings += finding(FindingCode.GAME_NOT_INSTALLED, FindingSeverity.BLOCKING)
            score -= 45
        }

        when {
            snapshot.thermalLevel.isDangerous -> {
                val blocksLaunch = profile == PerformanceProfile.EXTREME ||
                    snapshot.thermalLevel >= ThermalLevel.CRITICAL
                findings += finding(
                    FindingCode.THERMAL_DANGER,
                    if (blocksLaunch) FindingSeverity.BLOCKING else FindingSeverity.WARNING,
                )
                score -= if (blocksLaunch) 40 else 25
                launchAllowed = launchAllowed && !blocksLaunch
            }
            snapshot.thermalLevel == ThermalLevel.MODERATE -> {
                findings += finding(FindingCode.THERMAL_WARM, FindingSeverity.WARNING)
                score -= 12
            }
            snapshot.thermalLevel == ThermalLevel.UNSUPPORTED -> {
                findings += finding(FindingCode.LIMITED_SYSTEM_DATA, FindingSeverity.INFO)
                score -= 4
            }
            else -> Unit
        }

        val batteryTemperature = snapshot.batteryTemperatureCelsius
        if (batteryTemperature != null && batteryTemperature >= 45f) {
            val blocksLaunch = profile == PerformanceProfile.EXTREME
            findings += finding(
                FindingCode.BATTERY_TEMPERATURE_HIGH,
                if (blocksLaunch) FindingSeverity.BLOCKING else FindingSeverity.WARNING,
            )
            score -= if (blocksLaunch) 30 else 18
            launchAllowed = launchAllowed && !blocksLaunch
        }

        val memoryRatio = snapshot.availableMemoryRatio
        val availableMemory = snapshot.availableMemoryBytes
        if (snapshot.lowMemory ||
            (memoryRatio != null && memoryRatio < 0.12f) ||
            (availableMemory != null && availableMemory < 350L * 1024L * 1024L)
        ) {
            findings += finding(FindingCode.LOW_MEMORY, FindingSeverity.WARNING)
            score -= 18
        }

        if ((snapshot.batteryLevelPercent ?: 100) <= 15 && snapshot.isCharging != true) {
            findings += finding(FindingCode.LOW_BATTERY, FindingSeverity.WARNING)
            score -= 12
        }

        if (snapshot.isBatterySaverEnabled == true) {
            findings += finding(FindingCode.BATTERY_SAVER_ON, FindingSeverity.WARNING)
            score -= if (profile == PerformanceProfile.BALANCED) 5 else 10
        }

        if (findings.isEmpty()) {
            findings += finding(FindingCode.READY, FindingSeverity.PASS)
        }

        return BoostPlan(
            readinessScore = score.coerceIn(0, 100),
            launchAllowed = launchAllowed,
            findings = findings,
        )
    }

    private fun finding(code: FindingCode, severity: FindingSeverity) =
        ReadinessFinding(code = code, severity = severity)
}
