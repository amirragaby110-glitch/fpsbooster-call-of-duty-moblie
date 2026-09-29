package com.a21optimizer.domain

import com.a21optimizer.domain.model.GameCandidate
import com.a21optimizer.domain.model.GameRegion

object GameCatalog {
    // Explicit package visibility is required on Android 11+. These are the publisher packages
    // for the supported regional releases; arbitrary package scanning is intentionally avoided.
    val supportedGames: List<GameCandidate> = listOf(
        GameCandidate("com.activision.callofduty.shooter", GameRegion.GLOBAL),
        GameCandidate("com.garena.game.codm", GameRegion.GARENA),
        GameCandidate("com.vng.codmvn", GameRegion.VIETNAM),
        GameCandidate("com.tencent.tmgp.cod", GameRegion.CHINA),
    )

    val default: GameCandidate = supportedGames.first()

    fun byPackageName(packageName: String?): GameCandidate? =
        supportedGames.firstOrNull { it.packageName == packageName }
}
