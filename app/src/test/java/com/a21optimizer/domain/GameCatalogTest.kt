package com.a21optimizer.domain

import com.a21optimizer.domain.model.GameRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCatalogTest {
    @Test
    fun `official package candidates are unique and valid`() {
        val packages = GameCatalog.supportedGames.map { it.packageName }

        assertEquals(packages.size, packages.distinct().size)
        assertTrue(packages.all { it.matches(Regex("[a-zA-Z][a-zA-Z0-9_.]+")) })
        assertEquals(GameRegion.GLOBAL, GameCatalog.default.region)
    }

    @Test
    fun `lookup never accepts an arbitrary app`() {
        assertEquals(
            GameCatalog.default,
            GameCatalog.byPackageName("com.activision.callofduty.shooter"),
        )
        assertEquals(null, GameCatalog.byPackageName("com.example.not-a-game"))
    }
}
