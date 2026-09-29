package com.a21optimizer.domain.model

data class DeviceSnapshot(
    val totalMemoryBytes: Long? = null,
    val availableMemoryBytes: Long? = null,
    val lowMemory: Boolean = false,
    val batteryLevelPercent: Int? = null,
    val batteryTemperatureCelsius: Float? = null,
    val isCharging: Boolean? = null,
    val isBatterySaverEnabled: Boolean? = null,
    val thermalLevel: ThermalLevel = ThermalLevel.UNSUPPORTED,
    val cpuCoreCount: Int? = null,
    val deviceName: String = "Android device",
    val androidVersion: String = "Android",
    val refreshedAtMillis: Long = 0L,
) {
    val usedMemoryBytes: Long?
        get() = if (totalMemoryBytes != null && availableMemoryBytes != null) {
            (totalMemoryBytes - availableMemoryBytes).coerceAtLeast(0L)
        } else {
            null
        }

    val availableMemoryRatio: Float?
        get() = if (totalMemoryBytes != null && availableMemoryBytes != null && totalMemoryBytes > 0L) {
            (availableMemoryBytes.toDouble() / totalMemoryBytes.toDouble()).toFloat()
        } else {
            null
        }
}
