package com.example.managementsafetyvisit

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Global crash logger for any unhandled exception across threads
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("APP_CRASH", "==================================================")
            Log.e("APP_CRASH", "FATAL UNCAUGHT EXCEPTION in thread [${thread.name}]", throwable)
            Log.e("APP_CRASH", "==================================================")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}