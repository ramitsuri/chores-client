package com.ramitsuri.choresclient.data.settings

interface KeyValueStore {
    fun getString(key: String, fallback: String?): String?
    fun putString(key: String, value: String?)

    fun getStringList(key: String, fallback: List<String>): List<String>
    fun putStringList(key: String, value: List<String>)

    fun getLong(key: String, fallback: Long): Long
    fun putLong(key: String, value: Long)

    fun getInt(key: String, fallback: Int): Int
    fun putInt(key: String, value: Int)

    fun getBoolean(key: String, fallback: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)

    fun getFloat(key: String, fallback: Float): Float
    fun putFloat(key: String, value: Float)

    fun contains(key: String): Boolean
    fun remove(key: String)
    fun removeAll()
}