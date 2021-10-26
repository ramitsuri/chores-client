package com.ramitsuri.choresclient.android.di

import android.content.Context
import androidx.room.Room
import com.ramitsuri.choresclient.android.Base
import com.ramitsuri.choresclient.android.BuildConfig
import com.ramitsuri.choresclient.android.data.AppDatabase
import com.ramitsuri.choresclient.android.data.MemberDao
import com.ramitsuri.choresclient.android.data.ReminderAssignmentDao
import com.ramitsuri.choresclient.android.data.TaskAssignmentDao
import com.ramitsuri.choresclient.android.data.TaskDao
import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore
import com.ramitsuri.choresclient.android.keyvaluestore.PrefKeyValueStore
import com.ramitsuri.choresclient.android.notification.NotificationHandler
import com.ramitsuri.choresclient.android.notification.ReminderScheduler
import com.ramitsuri.choresclient.android.notification.SystemNotificationHandler
import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import com.ramitsuri.choresclient.android.reminder.SystemAlarmHandler
import com.ramitsuri.choresclient.android.utils.DefaultDispatchers
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import com.ramitsuri.choresclient.android.utils.getStartPeriodTime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideHttpClient() = HttpClient(Android) {

        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })

            engine {
                connectTimeout = Base.API_TIME_OUT
                socketTimeout = Base.API_TIME_OUT
            }
        }

        install(Logging) {
            logger = object: Logger {
                override fun log(message: String) {
                    Timber.tag("Logger Ktor =>")
                    Timber.v(message)
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse {response ->
                Timber.tag("HTTP status:")
                Timber.d("${response.status.value}")
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    @Singleton
    @Provides
    fun provideDefaultDispatchers(): DispatcherProvider = DefaultDispatchers()

    @Singleton
    @Provides
    fun provideBaseApiUrl() = if (BuildConfig.DEBUG) Base.API_BASE_URL_DEBUG else Base.API_BASE_URL

    @Singleton
    @Provides
    fun provideNotificationHandler(@ApplicationContext context: Context): NotificationHandler =
        SystemNotificationHandler(context)

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "app-database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideAlarmHandler(@ApplicationContext context: Context): AlarmHandler {
        return SystemAlarmHandler(context)
    }

    @Singleton
    @Provides
    fun provideReminderScheduler(
        reminderAssignmentDao: ReminderAssignmentDao,
        alarmHandler: AlarmHandler,
        dispatcherProvider: DispatcherProvider
    ): ReminderScheduler {
        return ReminderScheduler(reminderAssignmentDao, alarmHandler, dispatcherProvider) {
            getStartPeriodTime(it)
        }
    }

    @Singleton
    @Provides
    fun provideKeyValueStore(@ApplicationContext context: Context): KeyValueStore {
        return PrefKeyValueStore(context, "com.ramitsuri.choresclient.android.prefs")
    }

    @Singleton
    @Provides
    fun providePrefManager(keyValueStore: KeyValueStore): PrefManager {
        return PrefManager(keyValueStore)
    }

    @Provides
    fun provideAssignmentsDao(database: AppDatabase): TaskAssignmentDao {
        return database.taskAssignmentDao()
    }

    @Provides
    fun provideTasksDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideMembersDao(database: AppDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    fun provideRemindersDao(database: AppDatabase): ReminderAssignmentDao {
        return database.reminderAssignmentDao()
    }
}