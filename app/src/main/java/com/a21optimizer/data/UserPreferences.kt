package com.a21optimizer.data

import android.content.Context
import com.a21optimizer.domain.model.PerformanceProfile

class UserPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun profile(): PerformanceProfile =
        PerformanceProfile.fromStorageKey(preferences.getString(KEY_PROFILE, null))

    fun setProfile(profile: PerformanceProfile) {
        preferences.edit().putString(KEY_PROFILE, profile.storageKey).apply()
    }

    fun selectedPackage(): String? = preferences.getString(KEY_GAME_PACKAGE, null)

    fun setSelectedPackage(packageName: String) {
        preferences.edit().putString(KEY_GAME_PACKAGE, packageName).apply()
    }

    companion object {
        private const val FILE_NAME = "optimizer_preferences"
        private const val KEY_PROFILE = "performance_profile"
        private const val KEY_GAME_PACKAGE = "selected_game_package"
    }
}
