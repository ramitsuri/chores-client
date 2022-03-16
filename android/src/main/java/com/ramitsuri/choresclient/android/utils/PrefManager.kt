package com.ramitsuri.choresclient.android.utils

import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore
import java.util.concurrent.atomic.AtomicBoolean

class PrefManager(
    private val keyValueStore: KeyValueStore,
    private val secureKeyValueStore: KeyValueStore
) {

    init {
        deleteLegacyPrefs()
    }

    private var isWorkerRunning = AtomicBoolean(false)
    private val notificationIdLock = Any()

    fun setUserId(userId: String) {
        keyValueStore.put(USER_ID, userId)
    }

    fun getUserId(default: String? = null): String? {
        return keyValueStore.get(USER_ID, default)
    }

    fun setKey(key: String) {
        secureKeyValueStore.put(KEY, key)
    }

    fun getKey(default: String? = null): String? {
        return secureKeyValueStore.get(KEY, default)
    }

    fun setToken(token: String) {
        secureKeyValueStore.put(TOKEN, token)
    }

    fun getToken(default: String? = null): String? {
        return secureKeyValueStore.get(TOKEN, default)
    }

    fun setWorkerRunning(running: Boolean) {
        isWorkerRunning.set(running)
    }

    fun isWorkerRunning(): Boolean {
        return isWorkerRunning.get()
    }

    fun setDebugServer(server: String) {
        keyValueStore.put(DEBUG_SERVER, server)
    }

    fun getDebugServer(): String {
        return keyValueStore.get(DEBUG_SERVER, "") ?: ""
    }

    fun generateNewNotificationId(): Int {
        synchronized(notificationIdLock) {
            val newId = keyValueStore.get(PREV_NOTIFICATION_ID, 0) + 1
            keyValueStore.put(PREV_NOTIFICATION_ID, newId)
            return newId
        }
    }

    private fun deleteLegacyPrefs() {
        legacyPrefs.forEach { (key, store) ->
            if (store == KV) {
                keyValueStore.remove(key)
            } else if (store == SKV) {
                secureKeyValueStore.remove(key)
            }
        }
    }

    companion object {
        private const val USER_ID = "user_id"
        private const val KEY = "key"
        private const val TOKEN = "token"
        private const val DEBUG_SERVER = "debug_server"
        private const val PREV_NOTIFICATION_ID = "prev_notification_id"

        private const val KV = "KV"
        private const val SKV = "SKV"

        private val legacyPrefs = mapOf(
            "worker_running" to KV
        )
    }
}