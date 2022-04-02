package com.ramitsuri.choresclient.android.di

import android.content.Context
import com.ramitsuri.choresclient.android.BuildConfig
import com.ramitsuri.choresclient.android.notification.NotificationHandler
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.android.notification.SystemNotificationHandler
import com.ramitsuri.choresclient.android.reminder.SystemAlarmHandler
import com.ramitsuri.choresclient.android.utils.AppHelper
import com.ramitsuri.choresclient.utils.Base
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.db.DatabaseDriverFactory
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.entities.MemberDao
import com.ramitsuri.choresclient.data.entities.TaskAssignmentDao
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.data.notification.ReminderScheduler
import com.ramitsuri.choresclient.data.settings.KeyValueStore
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.data.settings.SecureSettingsKeyValueStore
import com.ramitsuri.choresclient.data.settings.SettingsKeyValueStore
import com.ramitsuri.choresclient.data.settings.SettingsProvider
import com.ramitsuri.choresclient.network.LoginApi
import com.ramitsuri.choresclient.network.TaskAssignmentsApi
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.AssignmentActionManager
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.SystemTaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentDataSource
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BearerTokens
import io.ktor.client.features.auth.providers.bearer
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideHttpClient(
        prefManager: PrefManager,
        baseUrl: String,
        dispatcherProvider: DispatcherProvider
    ) = HttpClient(Android) {

        val tokenClient = HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
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

        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = prefManager.getToken() ?: "",
                        refreshToken = ""
                    )
                }
                refreshTokens {
                    Timber.d("Token expired, refreshing")
                    val api = provideLoginApi(tokenClient, baseUrl)
                    val repo = provideLoginRepository(api, prefManager, dispatcherProvider)
                    repo.login(prefManager.getUserId() ?: "", prefManager.getKey() ?: "")
                    BearerTokens(
                        accessToken = prefManager.getToken() ?: "",
                        refreshToken = ""
                    )
                }
            }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("Logger Ktor =>")
                    Timber.v(message)
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
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
    fun provideDefaultDispatchers(): DispatcherProvider = DispatcherProvider()

    @Provides
    fun provideBaseApiUrl(prefManager: PrefManager) =
        if (BuildConfig.DEBUG) prefManager.getDebugServer() else Base.API_BASE_URL

    @Singleton
    @Provides
    fun provideNotificationHandler(@ApplicationContext context: Context): NotificationHandler =
        SystemNotificationHandler(context)

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context,
        dispatcherProvider: DispatcherProvider
    ): Database {
        return Database(DatabaseDriverFactory(context), dispatcherProvider)
    }

    @Singleton
    @Provides
    fun provideAlarmHandler(
        @ApplicationContext context: Context,
        alarmDao: AlarmDao
    ): AlarmHandler {
        return SystemAlarmHandler(ShowNotificationWorker.Companion, alarmDao, context)
    }

    @Singleton
    @Provides
    fun provideReminderScheduler(
        taskAssignmentsRepository: TaskAssignmentsRepository,
        prefManager: PrefManager,
        alarmHandler: AlarmHandler,
        dispatcherProvider: DispatcherProvider
    ): ReminderScheduler {
        return ReminderScheduler(
            taskAssignmentsRepository,
            alarmHandler,
            prefManager,
            dispatcherProvider
        )
    }

    @Pref
    @Singleton
    @Provides
    fun provideKeyValueStore(@ApplicationContext context: Context): KeyValueStore {
        val settingsProvider = SettingsProvider(
            "com.ramitsuri.choresclient.android.prefs",
            "com.ramitsuri.choresclient.android.normal",
            context
        )
        return SettingsKeyValueStore(settingsProvider.provide())
    }

    @SecurePref
    @Singleton
    @Provides
    fun provideSecureKeyValueStore(@ApplicationContext context: Context): KeyValueStore {
        val settingsProvider = SettingsProvider(
            "com.ramitsuri.choresclient.android.prefs",
            "com.ramitsuri.choresclient.android.normal",
            context
        )
        return SecureSettingsKeyValueStore(settingsProvider.provideSecure())
    }

    @Singleton
    @Provides
    fun providePrefManager(
        @Pref
        keyValueStore: KeyValueStore,
        @SecurePref
        securePrefKeyValueStore: KeyValueStore
    ): PrefManager {
        return PrefManager(keyValueStore, securePrefKeyValueStore)
    }

    @Provides
    fun provideAssignmentsDao(database: Database): TaskAssignmentDao {
        return database.taskAssignmentDao
    }

    @Provides
    fun provideTasksDao(database: Database): TaskDao {
        return database.taskDao
    }

    @Provides
    fun provideMembersDao(database: Database): MemberDao {
        return database.memberDao
    }

    @Provides
    fun provideAlarmDao(database: Database): AlarmDao {
        return database.alarmDao
    }

    @Provides
    fun provideAssignmentsApi(httpClient: HttpClient, baseUrl: String) =
        TaskAssignmentsApi(httpClient, baseUrl)

    @Provides
    fun provideAssignmentsDataSource(
        taskAssignmentDao: TaskAssignmentDao,
        memberDao: MemberDao,
        taskDao: TaskDao
    ): TaskAssignmentDataSource = TaskAssignmentDataSource(taskAssignmentDao, memberDao, taskDao)

    @Provides
    fun provideAssignmentsRepository(
        api: TaskAssignmentsApi,
        taskAssignmentDataSource: TaskAssignmentDataSource,
        dispatcherProvider: DispatcherProvider
    ): TaskAssignmentsRepository = SystemTaskAssignmentsRepository(
        api,
        taskAssignmentDataSource,
        dispatcherProvider
    )

    @Provides
    fun provideAssignmentsActionManager(
        coroutineScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
        taskAssignmentsRepository: TaskAssignmentsRepository,
        alarmHandler: AlarmHandler
    ) = AssignmentActionManager(
        coroutineScope,
        dispatcherProvider,
        taskAssignmentsRepository,
        alarmHandler
    )

    @Singleton
    @Provides
    fun provideLongLivingCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Singleton
    @Provides
    fun provideAppHelper(): AppHelper {
        return AppHelper()
    }

    @Provides
    fun provideLoginApi(httpClient: HttpClient, baseUrl: String) =
        LoginApi(httpClient, baseUrl)

    @Provides
    fun provideLoginRepository(
        api: LoginApi,
        prefManager: PrefManager,
        dispatcherProvider: DispatcherProvider
    ) = LoginRepository(
        api,
        prefManager,
        dispatcherProvider
    )
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecurePref

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Pref
