package com.ramitsuri.choresclient.data.db

import android.content.Context
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(ChoresDatabase.Schema, context, "chores.db")
    }
}