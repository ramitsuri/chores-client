package com.ramitsuri.choresclient.data.settings

import com.ramitsuri.choresclient.model.filter.Filter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class PrefManager(
    private val keyValueStore: KeyValueStore,
    private val secureKeyValueStore: KeyValueStore
) {

    init {
        deleteLegacyPrefs()
    }

    fun setLoggedInMemberId(userId: String) {
        val key = Key.LOGGED_IN_MEMBER_ID
        putString(key, userId)
    }

    fun getLoggedInMemberId(default: String? = null): String? {
        val key = Key.LOGGED_IN_MEMBER_ID
        return getString(key, default)
    }

    fun setKey(value: String) {
        val key = Key.KEY
        putString(key, value)
    }

    fun getKey(default: String? = null): String? {
        val key = Key.KEY
        return getString(key, default)
    }

    fun setToken(token: String) {
        val key = Key.TOKEN
        putString(key, token)
    }

    fun getToken(default: String? = null): String? {
        val key = Key.TOKEN
        return getString(key, default)
    }

    fun setDebugServer(server: String) {
        val key = Key.DEBUG_SERVER
        putString(key, server)
    }

    fun getDebugServer(): String {
        val key = Key.DEBUG_SERVER
        return getString(key, "") ?: ""
    }

    fun setEnableRemoteLogging(enable: Boolean) {
        val key = Key.ENABLE_REMOTE_LOGGING
        putBoolean(key, enable)
    }

    fun getEnableRemoteLogging(): Boolean {
        val key = Key.ENABLE_REMOTE_LOGGING
        return getBoolean(key, false)
    }

    fun getLastSyncTime(): Instant {
        val key = Key.LAST_SYNC_TIME
        val lastSyncTimeMillis = getLong(key, 0L)
        return Instant.fromEpochMilliseconds(lastSyncTimeMillis)
    }

    fun setLastSyncTime(time: Instant = Clock.System.now()) {
        val key = Key.LAST_SYNC_TIME
        val millis = time.toEpochMilliseconds()
        putLong(key, millis)
    }

    fun getSavedPersonFilterIds(): List<String> {
        val key = Key.SAVED_PERSON_FILTER_IDS
        return getStringList(key, listOf(Filter.ALL_ID))
    }

    fun setSavedPersonFilterIds(ids: List<String>) {
        val key = Key.SAVED_PERSON_FILTER_IDS
        putStringList(key, ids)
    }

    fun getSavedHouseFilterIds(): List<String> {
        val key = Key.SAVED_HOUSE_FILTER_IDS
        return getStringList(key, listOf(Filter.ALL_ID))
    }

    fun setSavedHouseFilterIds(ids: List<String>) {
        val key = Key.SAVED_HOUSE_FILTER_IDS
        putStringList(key, ids)
    }

    fun getEnabledNotificationActions(): List<String> {
        val key = Key.ENABLED_NOTIFICATION_ACTIONS
        return getStringList(
            key, listOf(
                "SNOOZE_HOUR",
                "SNOOZE_DAY",
                "COMPLETE"
            )
        )
    }

    fun setEnabledNotificationActions(value: List<String>) {
        val key = Key.ENABLED_NOTIFICATION_ACTIONS
        putStringList(key, value)
    }

    fun getDeviceId(): String? {
        val key = Key.DEVICE_ID
        return getString(key, null)
    }

    fun setDeviceId(value: String) {
        val key = Key.DEVICE_ID
        putString(key, value)
    }

    fun remindPastDue(): Boolean {
        val key = Key.REMIND_PAST_DUE
        return getBoolean(key, true)
    }

    fun setRemindPastDue(value: Boolean) {
        val key = Key.REMIND_PAST_DUE
        putBoolean(key, value)
    }

    fun useNewStyle(): Boolean {
        val key = Key.USE_NEW_STYLE
        return getBoolean(key, true)
    }

    fun setUseNewStyle(value: Boolean) {
        val key = Key.USE_NEW_STYLE
        putBoolean(key, value)
    }

    fun isLoggedIn(): Boolean {
        return (getKey().isNullOrEmpty() ||
                getLoggedInMemberId().isNullOrEmpty() ||
                getToken().isNullOrEmpty()).not()
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

    private fun putString(key: Key, value: String) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putString(key.key, value)
    }

    private fun getString(key: Key, default: String?): String? {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getString(key.key, default)
    }

    private fun putBoolean(key: Key, value: Boolean) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putBoolean(key.key, value)
    }

    private fun getBoolean(key: Key, default: Boolean): Boolean {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getBoolean(key.key, default)
    }

    private fun putInt(key: Key, value: Int) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putInt(key.key, value)
    }

    private fun getInt(key: Key, default: Int): Int {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getInt(key.key, default)
    }

    private fun putLong(key: Key, value: Long) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putLong(key.key, value)
    }

    private fun getLong(key: Key, default: Long): Long {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getLong(key.key, default)
    }

    private fun putStringList(key: Key, value: List<String>) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putStringList(key.key, value)
    }

    private fun getStringList(key: Key, default: List<String>): List<String> {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getStringList(key.key, default)
    }

    private fun getKeyValueStore(key: Key): KeyValueStore {
        return if (key.isSecure) {
            secureKeyValueStore
        } else {
            keyValueStore
        }
    }

    companion object {
        private const val KV = "KV"
        private const val SKV = "SKV"

        private val legacyPrefs = mapOf(
            "worker_running" to KV,
            "prev_notification_id" to KV
        )

        private enum class Key(val key: String, val isSecure: Boolean) {

            LOGGED_IN_MEMBER_ID(
                key = "logged_in_member_id",
                isSecure = false
            ),

            KEY(
                key = "key",
                isSecure = true
            ),

            TOKEN(
                key = "token",
                isSecure = true
            ),

            DEBUG_SERVER(
                key = "debug_server",
                isSecure = false
            ),

            ENABLE_REMOTE_LOGGING(
                key = "enable_remote_logging",
                isSecure = false
            ),

            LAST_SYNC_TIME(
                key = "last_sync_time",
                isSecure = false
            ),

            SAVED_PERSON_FILTER_IDS(
                key = "saved_person_filter_ids",
                isSecure = false
            ),

            SAVED_HOUSE_FILTER_IDS(
                key = "saved_house_filter_ids",
                isSecure = false
            ),

            ENABLED_NOTIFICATION_ACTIONS(
                key = "enabled_notification_actions",
                isSecure = false
            ),

            DEVICE_ID(
                key = "device_id",
                isSecure = false
            ),

            REMIND_PAST_DUE(
                key = "remind_past_due",
                isSecure = false
            ),

            USE_NEW_STYLE(
                key = "use_new_style",
                isSecure = false
            )
        }
    }
}