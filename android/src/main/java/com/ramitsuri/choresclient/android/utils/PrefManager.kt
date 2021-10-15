package com.ramitsuri.choresclient.android.utils

import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore

class PrefManager(private val keyValueStore: KeyValueStore) {

    fun setUserId(userId: String) {
        keyValueStore.put(USER_ID, userId)
    }

    fun getUserId(default: String? = null): String? {
        return keyValueStore.get(USER_ID, default)
    }

    companion object {
        private const val USER_ID = "user_id"
    }
}