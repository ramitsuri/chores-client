package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.Token
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.network.LoginApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext

class LoginRepository (
    private val api: LoginApi,
    private val prefManager: PrefManager,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun login(id: String, key: String): Result<Boolean> {
        return withContext(dispatcherProvider.io) {
            val result = try {
                api.login(id, key)
            } catch (e: Exception) {
                null
            }

            when {
                result == null -> {
                    Result.Failure(ViewError.NETWORK)
                }
                result.status == HttpStatusCode.OK -> {
                    val token: Token? = result.body()
                    token?.let {
                        prefManager.setToken(token.authToken)
                        prefManager.setUserId(id)
                        prefManager.setKey(key)
                        Result.Success(true)
                    } ?: run {
                        Result.Failure(ViewError.LOGIN_NO_TOKEN)
                    }
                }
                else -> {
                    Result.Failure(ViewError.LOGIN_REQUEST_FAILED)
                }
            }
        }
    }
}