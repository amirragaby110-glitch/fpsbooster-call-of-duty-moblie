package com.a21optimizer.domain.model

enum class PerformanceProfile(val storageKey: String) {
    BALANCED("balanced"),
    PERFORMANCE("performance"),
    EXTREME("extreme");

    companion object {
        fun fromStorageKey(value: String?): PerformanceProfile =
            entries.firstOrNull { it.storageKey == value } ?: BALANCED
    }
}
