package com.ramitsuri.choresclient.network

import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.utils.Base
import com.ramitsuri.choresclient.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

class NetworkProvider(
    private val prefManager: PrefManager,
    private val isDebug: Boolean,
    clientEngine: HttpClientEngine,
    dispatcherProvider: DispatcherProvider
) {

    private val log: KermitLogger = KermitLogger(
        StaticConfig(logWriterList = listOf(platformLogWriter())),
        "Ktor"
    )
    private val client = HttpClient(clientEngine) {

        val tokenClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            val timeout = Base.API_TIME_OUT.toLong()
            connectTimeoutMillis = timeout
            requestTimeoutMillis = timeout
            socketTimeoutMillis = timeout
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
                    log.d("Token expired, refreshing")
                    val api = provideLoginApi(tokenClient)
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
            logger = object : KtorLogger {
                override fun log(message: String) {
                    log.v { message }
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                log.d { "${response.status.value}" }
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    fun provideLoginRepository(
        api: LoginApi = provideLoginApi(client),
        prefManager: PrefManager,
        dispatcherProvider: DispatcherProvider
    ): LoginRepository {
        return LoginRepository(
            api,
            prefManager,
            dispatcherProvider
        )
    }

    fun provideAssignmentsApi() =
        TaskAssignmentsApi(client, provideBaseApiUrl())

    fun provideSyncApi() =
        SyncApi(client, provideBaseApiUrl())

    fun provideTasksApi() =
        TasksApi(client, provideBaseApiUrl())

    fun providePushMessageTokenApi(httpClient: HttpClient = client) =
        PushMessageTokenApi(httpClient, provideBaseApiUrl())

    private fun provideLoginApi(httpClient: HttpClient = client) =
        LoginApi(httpClient, provideBaseApiUrl())

    private fun provideBaseApiUrl() =
        if (isDebug) prefManager.getDebugServer() else Base.API_BASE_URL
}