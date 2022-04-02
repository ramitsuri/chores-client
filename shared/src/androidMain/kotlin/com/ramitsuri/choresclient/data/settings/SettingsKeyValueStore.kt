package com.ramitsuri.choresclient.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.AndroidSettings
import com.russhwolf.settings.Settings

actual class SettingsProvider(
    private val fileName: String,
    private val secureFileName: String,
    private val context: Context
) {
    actual fun provide(): Settings {
        val sharedPrefs =
            context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return AndroidSettings(sharedPrefs)
    }

    actual fun provideSecure(): Settings {
        val mainKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val preferences: SharedPreferences = EncryptedSharedPreferences.create(
            context.applicationContext,
            secureFileName,
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return AndroidSettings(preferences)
    }
}