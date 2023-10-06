package com.ramitsuri.choresclient.utils

interface AppLifecycleObserver {
    val isAppInForeground: Boolean

    fun init()
}