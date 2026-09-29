package com.a21optimizer.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.a21optimizer.domain.model.GameCandidate
import com.a21optimizer.domain.model.GameInstallation

class AndroidGameRepository(
    context: Context,
) : GameRepository {
    private val packageManager = context.packageManager

    override fun installations(candidates: List<GameCandidate>): List<GameInstallation> =
        candidates.map { candidate ->
            val info = applicationInfo(candidate.packageName)
            GameInstallation(
                candidate = candidate,
                isInstalled = info?.enabled == true,
                appLabel = info?.let { runCatching { packageManager.getApplicationLabel(it).toString() }.getOrNull() },
            )
        }

    private fun applicationInfo(packageName: String): ApplicationInfo? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0L),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, 0)
        }
    }.getOrNull()
}
