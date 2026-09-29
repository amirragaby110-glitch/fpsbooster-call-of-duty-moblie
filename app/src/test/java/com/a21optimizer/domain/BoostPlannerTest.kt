package com.a21optimizer.domain

import com.a21optimizer.domain.model.DeviceSnapshot
import com.a21optimizer.domain.model.FindingCode
import com.a21optimizer.domain.model.FindingSeverity
import com.a21optimizer.domain.model.GameInstallation
import com.a21optimizer.domain.model.PerformanceProfile
import com.a21optimizer.domain.model.ThermalLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoostPlannerTest {
    private val planner = BoostPlanner()
    private val installedGame = GameInstallation(GameCatalog.default, isInstalled = true)
    private val healthySnapshot = DeviceSnapshot(
        totalMemoryBytes = 4L * GIB,
        availableMemoryBytes = 2L * GIB,
        batteryLevelPercent = 75,
        batteryTemperatureCelsius = 32f,
        isCharging = false,
        isBatterySaverEnabled = false,
        thermalLevel = ThermalLevel.NONE,
    )

    @Test
    fun `healthy device is ready without invented optimizations`() {
        val plan = planner.createPlan(
            healthySnapshot,
            installedGame,
            PerformanceProfile.PERFORMANCE,
        )

        assertTrue(plan.launchAllowed)
        assertEquals(100, plan.readinessScore)
        assertEquals(FindingCode.READY, plan.findings.single().code)
    }

    @Test
    fun `missing game always blocks launch`() {
        val plan = planner.createPlan(
            healthySnapshot,
            installedGame.copy(isInstalled = false),
            PerformanceProfile.BALANCED,
        )

        assertFalse(plan.launchAllowed)
        assertTrue(plan.findings.any { it.code == FindingCode.GAME_NOT_INSTALLED })
    }

    @Test
    fun `severe thermal pressure blocks extreme but warns performance`() {
        val hot = healthySnapshot.copy(thermalLevel = ThermalLevel.SEVERE)

        val extreme = planner.createPlan(hot, installedGame, PerformanceProfile.EXTREME)
        val performance = planner.createPlan(hot, installedGame, PerformanceProfile.PERFORMANCE)

        assertFalse(extreme.launchAllowed)
        assertEquals(
            FindingSeverity.BLOCKING,
            extreme.findings.first { it.code == FindingCode.THERMAL_DANGER }.severity,
        )
        assertTrue(performance.launchAllowed)
        assertEquals(
            FindingSeverity.WARNING,
            performance.findings.first { it.code == FindingCode.THERMAL_DANGER }.severity,
        )
    }

    @Test
    fun `critical thermal pressure blocks every profile`() {
        PerformanceProfile.entries.forEach { profile ->
            val plan = planner.createPlan(
                healthySnapshot.copy(thermalLevel = ThermalLevel.CRITICAL),
                installedGame,
                profile,
            )
            assertFalse("$profile must respect Android thermal safety", plan.launchAllowed)
        }
    }

    @Test
    fun `high battery temperature blocks extreme profile`() {
        val plan = planner.createPlan(
            healthySnapshot.copy(batteryTemperatureCelsius = 46f),
            installedGame,
            PerformanceProfile.EXTREME,
        )

        assertFalse(plan.launchAllowed)
        assertTrue(plan.findings.any { it.code == FindingCode.BATTERY_TEMPERATURE_HIGH })
    }

    @Test
    fun `low memory low battery and saver are reported together`() {
        val constrained = healthySnapshot.copy(
            availableMemoryBytes = 250L * MIB,
            batteryLevelPercent = 10,
            isBatterySaverEnabled = true,
        )
        val plan = planner.createPlan(
            constrained,
            installedGame,
            PerformanceProfile.PERFORMANCE,
        )

        assertTrue(plan.launchAllowed)
        assertTrue(plan.findings.any { it.code == FindingCode.LOW_MEMORY })
        assertTrue(plan.findings.any { it.code == FindingCode.LOW_BATTERY })
        assertTrue(plan.findings.any { it.code == FindingCode.BATTERY_SAVER_ON })
        assertTrue(plan.readinessScore < 100)
    }

    private companion object {
        const val MIB = 1024L * 1024L
        const val GIB = 1024L * MIB
    }
}
