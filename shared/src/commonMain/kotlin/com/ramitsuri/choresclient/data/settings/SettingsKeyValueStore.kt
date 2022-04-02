package com.ramitsuri.choresclient.data.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class SettingsKeyValueStore(private val settings: Settings) : KeyValueStore {
    override fun get(key: String, fallback: String?): String? {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: String?) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Long): Long {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Long) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Int): Int {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Int) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Boolean): Boolean {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Boolean) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Float): Float {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Float) {
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

class SecureSettingsKeyValueStore(private val settings: Settings) : KeyValueStore {
    override fun get(key: String, fallback: String?): String? {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: String?) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Long): Long {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Long) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Int): Int {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Int) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Boolean): Boolean {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Boolean) {
        settings[key] = value
    }

    override fun get(key: String, fallback: Float): Float {
        return settings[key] ?: fallback
    }

    override fun put(key: String, value: Float) {
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

expect class SettingsProvider {
    fun provide(): Settings
    fun provideSecure(): Settings
}
