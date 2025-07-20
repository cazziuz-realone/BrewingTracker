package com.brewingtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BrewingTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}