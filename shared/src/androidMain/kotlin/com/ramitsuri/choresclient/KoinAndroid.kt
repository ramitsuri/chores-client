package com.ramitsuri.choresclient

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.data.settings.SettingsKeyValueStore
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.russhwolf.settings.AndroidSettings
import com.squareup.sqldelight.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.dsl.module

actual val platformModule = module {
    factory<HttpClientEngine> {
        Android.create()
    }

    single<DispatcherProvider> {
        DispatcherProvider()
    }

    single<Database> {
        Database(
            AndroidSqliteDriver(
                ChoresDatabase.Schema,
                get<Context>(), "chores.db"
            ),
            get<DispatcherProvider>()
        )
    }

    single<PrefManager> {
        val context: Context = get()
        val fileName = "com.ramitsuri.choresclient.android.prefs"
        val secureFileName = "com.ramitsuri.choresclient.android.normal"

        val sharedPrefs =
            context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val keyValueStore = SettingsKeyValueStore(AndroidSettings(sharedPrefs))

        val mainKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val secureSharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
            context.applicationContext,
            secureFileName,
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val secureKeyValueStore = SettingsKeyValueStore(AndroidSettings(secureSharedPrefs))

        PrefManager(keyValueStore, secureKeyValueStore)
    }

    single<AppHelper> {
        AppHelper()
    }
}