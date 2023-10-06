package com.ramitsuri.choresclient.android.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ramitsuri.choresclient.utils.AppLifecycleObserver

class DefaultAppLifecycleObserver : AppLifecycleObserver, LifecycleEventObserver {
    private var isForeground = false

    override fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            isForeground = true
        }
        if (event == Lifecycle.Event.ON_STOP) {
            isForeground = false
        }
    }

    override val isAppInForeground: Boolean
        get() = isForeground
}