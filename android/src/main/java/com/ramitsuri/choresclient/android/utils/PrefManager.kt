package com.ramitsuri.choresclient.android.utils

import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore

class PrefManager(
    private val keyValueStore: KeyValueStore,
    private val secureKeyValueStore: KeyValueStore
) {

    private val runningLock = Any()

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

    companion object {
        private const val USER_ID = "user_id"
        private const val KEY = "key"
        private const val TOKEN = "token"
        private const val WORKER_RUNNING = "worker_running"
    }
}