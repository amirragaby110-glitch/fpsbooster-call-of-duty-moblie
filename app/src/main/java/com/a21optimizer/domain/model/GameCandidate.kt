package com.a21optimizer.domain.model

data class GameCandidate(
    val packageName: String,
    val region: GameRegion,
)

enum class GameRegion {
    GLOBAL,
    GARENA,
    VIETNAM,
    CHINA,
}

data class GameInstallation(
    val candidate: GameCandidate,
    val isInstalled: Boolean,
    val appLabel: String? = null,
)
