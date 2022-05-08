package com.ramitsuri.choresclient.data.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class SettingsKeyValueStore(private val settings: Settings) : KeyValueStore {
    override fun getString(key: String, fallback: String?): String? {
        return settings[key] ?: fallback
    }

    override fun putString(key: String, value: String?) {
        settings[key] = value
    }

    override fun getLong(key: String, fallback: Long): Long {
        return settings[key] ?: fallback
    }

    override fun putLong(key: String, value: Long) {
        settings[key] = value
    }

    override fun getInt(key: String, fallback: Int): Int {
        return settings[key] ?: fallback
    }

    override fun putInt(key: String, value: Int) {
        settings[key] = value
    }

    override fun getBoolean(key: String, fallback: Boolean): Boolean {
        return settings[key] ?: fallback
    }

    override fun putBoolean(key: String, value: Boolean) {
        settings[key] = value
    }

    override fun getFloat(key: String, fallback: Float): Float {
        return settings[key] ?: fallback
    }

    override fun putFloat(key: String, value: Float) {
        settings[key] = value
    }

    override fun contains(key: String): Boolean {
        return settings.contains(key)
    }

    override fun remove(key: String) {
        settings.remove(key)
    }

    override fun removeAll() {
        settings.clear()
    }
}

