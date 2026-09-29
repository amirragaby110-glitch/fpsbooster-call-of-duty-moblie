package com.a21optimizer

import android.app.Application

class OptimizerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLog.debug("Application started; no background service is scheduled")
    }
}
