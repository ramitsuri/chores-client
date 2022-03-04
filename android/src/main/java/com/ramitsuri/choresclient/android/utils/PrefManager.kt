package com.ramitsuri.choresclient.android.utils

import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore

class PrefManager(
    private val keyValueStore: KeyValueStore,
    private val secureKeyValueStore: KeyValueStore
) {

    private val runningLock = Any()
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
        synchronized(runningLock) {
            keyValueStore.put(WORKER_RUNNING, running)
        }
    }

    fun isWorkerRunning(default: Boolean = false): Boolean {
        synchronized(runningLock) {
            return keyValueStore.get(WORKER_RUNNING, default)
        }
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

    companion object {
        private const val USER_ID = "user_id"
        private const val KEY = "key"
        private const val TOKEN = "token"
        private const val WORKER_RUNNING = "worker_running"
        private const val DEBUG_SERVER = "debug_server"
        private const val PREV_NOTIFICATION_ID = "prev_notification_id"
    }
}