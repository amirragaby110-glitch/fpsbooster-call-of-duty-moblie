package com.a21optimizer.data

import com.a21optimizer.domain.model.GameCandidate
import com.a21optimizer.domain.model.GameInstallation

interface GameRepository {
    fun installations(candidates: List<GameCandidate>): List<GameInstallation>
}
