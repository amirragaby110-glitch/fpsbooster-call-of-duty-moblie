package com.a21optimizer.data

import com.a21optimizer.domain.model.DeviceSnapshot
import com.a21optimizer.domain.model.ThermalLevel
import kotlinx.coroutines.flow.Flow

interface DeviceStatusRepository {
    fun readSnapshot(): DeviceSnapshot
    fun observeThermalStatus(): Flow<ThermalLevel>
}
