package com.a21optimizer.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.a21optimizer.domain.model.DeviceSnapshot
import com.a21optimizer.domain.model.ThermalLevel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import java.util.Locale

class AndroidDeviceStatusRepository(
    private val context: Context,
) : DeviceStatusRepository {
    private val activityManager: ActivityManager?
        get() = context.getSystemService(ActivityManager::class.java)
    private val powerManager: PowerManager?
        get() = context.getSystemService(PowerManager::class.java)

    override fun readSnapshot(): DeviceSnapshot {
        val memoryInfo = runCatching {
            ActivityManager.MemoryInfo().also { activityManager?.getMemoryInfo(it) }
        }.getOrNull()
        val batteryIntent = readBatteryIntent()
        val level = batteryIntent?.intExtra(BatteryManager.EXTRA_LEVEL)
        val scale = batteryIntent?.intExtra(BatteryManager.EXTRA_SCALE)
        val batteryPercent = if (level != null && scale != null && level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            null
        }
        val rawTemperature = batteryIntent?.intExtra(BatteryManager.EXTRA_TEMPERATURE)
        val batteryTemperature = rawTemperature
            ?.takeIf { it != Int.MIN_VALUE && it > 0 }
            ?.div(10f)
        val status = batteryIntent?.intExtra(BatteryManager.EXTRA_STATUS)
        val charging = status?.let {
            it == BatteryManager.BATTERY_STATUS_CHARGING ||
                it == BatteryManager.BATTERY_STATUS_FULL
        }
        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { ThermalLevel.fromStatusCode(powerManager?.currentThermalStatus) }
                .getOrDefault(ThermalLevel.UNSUPPORTED)
        } else {
            ThermalLevel.UNSUPPORTED
        }

        return DeviceSnapshot(
            totalMemoryBytes = memoryInfo?.totalMem?.takeIf { it > 0L },
            availableMemoryBytes = memoryInfo?.availMem?.takeIf { it >= 0L },
            lowMemory = memoryInfo?.lowMemory ?: false,
            batteryLevelPercent = batteryPercent,
            batteryTemperatureCelsius = batteryTemperature,
            isCharging = charging,
            isBatterySaverEnabled = runCatching { powerManager?.isPowerSaveMode }.getOrNull(),
            thermalLevel = thermal,
            cpuCoreCount = runCatching { Runtime.getRuntime().availableProcessors() }
                .getOrNull()
                ?.takeIf { it > 0 },
            deviceName = deviceName(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            refreshedAtMillis = System.currentTimeMillis(),
        )
    }

    override fun observeThermalStatus(): Flow<ThermalLevel> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return flowOf(ThermalLevel.UNSUPPORTED)
        }
        val manager = powerManager ?: return flowOf(ThermalLevel.UNSUPPORTED)
        return callbackFlow {
            val listener = PowerManager.OnThermalStatusChangedListener { status ->
                trySend(ThermalLevel.fromStatusCode(status))
            }
            runCatching {
                trySend(ThermalLevel.fromStatusCode(manager.currentThermalStatus))
                manager.addThermalStatusListener(listener)
            }.onFailure {
                trySend(ThermalLevel.UNSUPPORTED)
                close(it)
            }
            awaitClose { runCatching { manager.removeThermalStatusListener(listener) } }
        }
    }

    private fun readBatteryIntent(): Intent? = runCatching {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(null, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(null, filter)
        }
    }.getOrNull()

    private fun deviceName(): String {
        val manufacturer = Build.MANUFACTURER.orEmpty().trim()
        val model = Build.MODEL.orEmpty().trim()
        val combined = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            listOf(manufacturer, model).filter { it.isNotBlank() }.joinToString(" ")
        }
        return combined.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }.ifBlank { "Android device" }
    }

    private fun Intent.intExtra(key: String): Int = getIntExtra(key, Int.MIN_VALUE)
}
