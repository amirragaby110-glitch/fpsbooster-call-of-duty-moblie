package com.a21optimizer.domain.model

enum class ThermalLevel(val statusCode: Int, val isDangerous: Boolean) {
    NONE(0, false),
    LIGHT(1, false),
    MODERATE(2, false),
    SEVERE(3, true),
    CRITICAL(4, true),
    EMERGENCY(5, true),
    SHUTDOWN(6, true),
    UNSUPPORTED(-1, false);

    companion object {
        fun fromStatusCode(code: Int?): ThermalLevel =
            entries.firstOrNull { it.statusCode == code } ?: UNSUPPORTED
    }
}
