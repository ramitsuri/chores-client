package com.ramitsuri.choresclient.data.settings

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class SettingsProvider(private val delegate: NSUserDefaults) {
    actual fun provide(): Settings {
        return AppleSettings(NSUserDefaults.standardUserDefaults)
    }

    @OptIn(ExperimentalSettingsImplementation::class)
    actual fun provideSecure(): Settings {
        return KeychainSettings()
    }
}