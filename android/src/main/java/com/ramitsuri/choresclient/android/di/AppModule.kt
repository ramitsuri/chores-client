package com.ramitsuri.choresclient.android.di

import android.content.Context
import androidx.room.Room
import com.ramitsuri.choresclient.android.BuildConfig
import com.ramitsuri.choresclient.android.data.AlarmDao
import com.ramitsuri.choresclient.android.data.AppDatabase
import com.ramitsuri.choresclient.android.data.MemberDao
import com.ramitsuri.choresclient.android.data.TaskAssignmentDao
import com.ramitsuri.choresclient.android.data.TaskAssignmentDataSource
import com.ramitsuri.choresclient.android.data.TaskDao
import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore
import com.ramitsuri.choresclient.android.keyvaluestore.PrefKeyValueStore
import com.ramitsuri.choresclient.android.keyvaluestore.SecurePrefKeyValueStore
import com.ramitsuri.choresclient.android.network.TaskAssignmentsApi
import com.ramitsuri.choresclient.android.notification.NotificationHandler
import com.ramitsuri.choresclient.android.notification.ReminderScheduler
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.android.notification.SystemNotificationHandler
import com.ramitsuri.choresclient.android.reminder.AlarmHandler
import com.ramitsuri.choresclient.android.reminder.SystemAlarmHandler
import com.ramitsuri.choresclient.android.repositories.AssignmentActionManager
import com.ramitsuri.choresclient.android.repositories.SystemTaskAssignmentsRepository
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.Base
import com.ramitsuri.choresclient.android.utils.DefaultDispatchers
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
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
                    val module = LoginModule()
                    val api = module.provideLoginApi(tokenClient, baseUrl)
                    val repo = module.provideLoginRepository(api, prefManager, dispatcherProvider)
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
    fun provideDefaultDispatchers(): DispatcherProvider = DefaultDispatchers()

    @Provides
    fun provideBaseApiUrl(prefManager: PrefManager) =
        if (BuildConfig.DEBUG) prefManager.getDebugServer() else Base.API_BASE_URL

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
        return PrefKeyValueStore(context, "com.ramitsuri.choresclient.android.prefs")
    }

    @SecurePref
    @Singleton
    @Provides
    fun provideSecureKeyValueStore(@ApplicationContext context: Context): KeyValueStore {
        return SecurePrefKeyValueStore(context, "com.ramitsuri.choresclient.android.normal")
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
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
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
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecurePref

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Pref
