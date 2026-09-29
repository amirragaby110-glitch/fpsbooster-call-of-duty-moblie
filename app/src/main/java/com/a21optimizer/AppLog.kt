package com.a21optimizer

import android.util.Log

object AppLog {
    private const val TAG = "A21Optimizer"

    fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(TAG, message, throwable)
    }
}
