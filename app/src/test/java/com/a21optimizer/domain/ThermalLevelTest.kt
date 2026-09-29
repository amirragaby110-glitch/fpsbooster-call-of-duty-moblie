package com.a21optimizer.domain

import com.a21optimizer.domain.model.ThermalLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThermalLevelTest {
    @Test
    fun `platform statuses map without guessing unknown values`() {
        assertEquals(ThermalLevel.NONE, ThermalLevel.fromStatusCode(0))
        assertEquals(ThermalLevel.SEVERE, ThermalLevel.fromStatusCode(3))
        assertEquals(ThermalLevel.SHUTDOWN, ThermalLevel.fromStatusCode(6))
        assertEquals(ThermalLevel.UNSUPPORTED, ThermalLevel.fromStatusCode(99))
        assertEquals(ThermalLevel.UNSUPPORTED, ThermalLevel.fromStatusCode(null))
    }

    @Test
    fun `only severe and above are dangerous`() {
        assertFalse(ThermalLevel.MODERATE.isDangerous)
        assertTrue(ThermalLevel.SEVERE.isDangerous)
        assertTrue(ThermalLevel.CRITICAL.isDangerous)
    }
}
