package com.ramitsuri.choresclient.android

import android.app.Application
import com.ramitsuri.choresclient.android.di.AppModule
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    lateinit var appContainer: AppModule

    override fun onCreate() {
        super.onCreate()
        appContainer = AppModule()
    }
}