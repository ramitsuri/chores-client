package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.data.settings.KeyValueStore

class FakeKeyValueStore : KeyValueStore {

    private val store = mutableMapOf<String, Any?>()

    override fun get(key: String, fallback: String?): String? {
        val value = store[key]
        return if (value is String?) {
            value
        } else {
            fallback
        }
    }

    override fun put(key: String, value: String?) {
        store[key] = value
    }

    override fun get(key: String, fallback: Long): Long {
        val value = store[key]
        return if (value is Long) {
            value
        } else {
            fallback
        }
    }

    override fun put(key: String, value: Long) {
        store[key] = value
    }

    override fun get(key: String, fallback: Int): Int {
        val value = store[key]
        return if (value is Int) {
            value
        } else {
            fallback
        }
    }

    override fun put(key: String, value: Int) {
        store[key] = value
    }

    override fun get(key: String, fallback: Boolean): Boolean {
        val value = store[key]
        return if (value is Boolean) {
            value
        } else {
            fallback
        }
    }

    override fun put(key: String, value: Boolean) {
        store[key] = value
    }

    override fun get(key: String, fallback: Float): Float {
        val value = store[key]
        return if (value is Float) {
            value
        } else {
            fallback
        }
    }

    override fun put(key: String, value: Float) {
        store[key] = value
    }

    override fun contains(key: String): Boolean {
        return store[key] != null
    }

    override fun remove(key: String) {
        store.remove(key)
    }

    override fun removeAll() {
        store.clear()
    }
}