package com.ramitsuri.choresclient.android.keyvaluestore

interface KeyValueStore {
    fun get(key: String, fallback: String?): String?
    fun put(key: String, value: String?)

    fun get(key: String, fallback: Long): Long
    fun put(key: String, value: Long)

    fun get(key: String, fallback: Int): Int
    fun put(key: String, value: Int)

    fun get(key: String, fallback: Boolean): Boolean
    fun put(key: String, value: Boolean)

    fun get(key: String, fallback: Float): Float
    fun put(key: String, value: Float)

    fun contains(key: String): Boolean
    fun remove(key: String)
    fun removeAll()
}