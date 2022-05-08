package com.ramitsuri.choresclient.data.settings

import com.ramitsuri.choresclient.utils.Lock
import com.ramitsuri.choresclient.utils.use

class PrefManager(
    private val keyValueStore: KeyValueStore,
    private val secureKeyValueStore: KeyValueStore
) {

    init {
        deleteLegacyPrefs()
    }

    private val notificationIdLock = Lock()

    fun setUserId(userId: String) {
        keyValueStore.putString(USER_ID, userId)
    }

    fun getUserId(default: String? = null): String? {
        return keyValueStore.getString(USER_ID, default)
    }

    fun setKey(key: String) {
        secureKeyValueStore.putString(KEY, key)
    }

    fun getKey(default: String? = null): String? {
        return secureKeyValueStore.getString(KEY, default)
    }

    fun setToken(token: String) {
        secureKeyValueStore.putString(TOKEN, token)
    }

    fun getToken(default: String? = null): String? {
        return secureKeyValueStore.getString(TOKEN, default)
    }

    fun setDebugServer(server: String) {
        keyValueStore.putString(DEBUG_SERVER, server)
    }

    fun getDebugServer(): String {
        return keyValueStore.getString(DEBUG_SERVER, "") ?: ""
    }

    fun generateNewNotificationId(): Int {
        notificationIdLock.use {
            val newId = keyValueStore.getInt(PREV_NOTIFICATION_ID, 0) + 1
            keyValueStore.putInt(PREV_NOTIFICATION_ID, newId)
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