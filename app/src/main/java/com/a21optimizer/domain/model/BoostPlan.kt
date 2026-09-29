package com.a21optimizer.domain.model

data class BoostPlan(
    val readinessScore: Int,
    val launchAllowed: Boolean,
    val findings: List<ReadinessFinding>,
)

data class ReadinessFinding(
    val code: FindingCode,
    val severity: FindingSeverity,
)

enum class FindingSeverity {
    PASS,
    INFO,
    WARNING,
    BLOCKING,
}

enum class FindingCode {
    READY,
    GAME_NOT_INSTALLED,
    THERMAL_DANGER,
    THERMAL_WARM,
    BATTERY_TEMPERATURE_HIGH,
    LOW_MEMORY,
    LOW_BATTERY,
    BATTERY_SAVER_ON,
    LIMITED_SYSTEM_DATA,
}
