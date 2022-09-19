package com.ramitsuri.choresclient.data.settings

import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.utils.Lock
import com.ramitsuri.choresclient.utils.use
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

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

    fun setEnableRemoteLogging(enable: Boolean) {
        keyValueStore.putBoolean(ENABLE_REMOTE_LOGGING, enable)
    }

    fun getEnableRemoteLogging(): Boolean {
        return keyValueStore.getBoolean(ENABLE_REMOTE_LOGGING, false)
    }

    fun generateNewNotificationId(): Int {
        notificationIdLock.use {
            val newId = keyValueStore.getInt(PREV_NOTIFICATION_ID, 0) + 1
            keyValueStore.putInt(PREV_NOTIFICATION_ID, newId)
            return newId
        }
    }

    fun getLastSyncTime(): Instant {
        val lastSyncTimeMillis = keyValueStore.getLong(LAST_SYNC_TIME, 0L)
        return Instant.fromEpochMilliseconds(lastSyncTimeMillis)
    }

    fun setLastSyncTime(time: Instant = Clock.System.now()) {
        val millis = time.toEpochMilliseconds()
        keyValueStore.putLong(LAST_SYNC_TIME, millis)
    }

    fun getSavedPersonFilterIds(): List<String> {
        return keyValueStore.getStringList(SAVED_PERSON_FILTER_IDS, listOf(Filter.ALL_ID))
    }

    fun setSavedPersonFilterIds(ids: List<String>) {
        keyValueStore.putStringList(SAVED_PERSON_FILTER_IDS, ids)
    }

    fun getSavedHouseFilterIds(): List<String> {
        return keyValueStore.getStringList(SAVED_HOUSE_FILTER_IDS, listOf(Filter.ALL_ID))
    }

    fun setSavedHouseFilterIds(ids: List<String>) {
        keyValueStore.putStringList(SAVED_HOUSE_FILTER_IDS, ids)
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
        private const val ENABLE_REMOTE_LOGGING = "enable_remote_logging"
        private const val PREV_NOTIFICATION_ID = "prev_notification_id"
        private const val LAST_SYNC_TIME = "last_sync_time"
        private const val SAVED_PERSON_FILTER_IDS = "saved_person_filter_ids"
        private const val SAVED_HOUSE_FILTER_IDS = "saved_house_filter_ids"

        private const val KV = "KV"
        private const val SKV = "SKV"

        private val legacyPrefs = mapOf(
            "worker_running" to KV
        )
    }
}