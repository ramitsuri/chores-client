package com.ramitsuri.choresclient.android.keyvaluestore

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PrefKeyValueStore(context: Context, fileName: String): KeyValueStore {
    private val preferences =
        context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    override fun get(key: String, fallback: String?): String? {
        return try {
            preferences.getString(key, fallback)
        } catch (e: ClassCastException) {
            fallback
        }
    }

    override fun put(key: String, value: String?) {
        preferences.editAndApply {
            putString(key, value)
        }
    }

    override fun get(key: String, fallback: Long): Long {
        return try {
            preferences.getLong(key, fallback)
        } catch (e: ClassCastException) {
            fallback
        }
    }

    override fun put(key: String, value: Long) {
        preferences.editAndApply {
            putLong(key, value)
        }
    }

    override fun get(key: String, fallback: Int): Int {
        return try {
            preferences.getInt(key, fallback)
        } catch (e: ClassCastException) {
            fallback
        }
    }

    override fun put(key: String, value: Int) {
        preferences.editAndApply {
            putInt(key, value)
        }
    }

    override fun get(key: String, fallback: Boolean): Boolean {
        return try {
            preferences.getBoolean(key, fallback)
        } catch (e: ClassCastException) {
            fallback
        }
    }

    override fun put(key: String, value: Boolean) {
        preferences.editAndApply {
            putBoolean(key, value)
        }
    }

    override fun get(key: String, fallback: Float): Float {
        return try {
            preferences.getFloat(key, fallback)
        } catch (e: ClassCastException) {
            fallback
        }
    }

    override fun put(key: String, value: Float) {
        preferences.editAndApply {
            putFloat(key, value)
        }
    }

    override fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    override fun remove(key: String) {
        preferences.editAndApply {
            remove(key)
        }
    }

    override fun removeAll() {
        preferences.editAndApply {
            clear()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun SharedPreferences.editAndApply(
        block: SharedPreferences.Editor.() -> SharedPreferences.Editor
    ) {
        this.edit()
            .block()
            .apply()
    }
}

class KeyValueStoreFactory {
    companion object {
        fun create(context: Context, kvFileName: String): KeyValueStore {
            return PrefKeyValueStore(context, kvFileName)
        }
    }
}