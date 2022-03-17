package com.ramitsuri.choresclient.android.repositories

import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.Token
import com.ramitsuri.choresclient.android.model.ViewError
import com.ramitsuri.choresclient.android.network.LoginApi
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import io.ktor.client.call.receive
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import kotlinx.coroutines.withContext
import timber.log.Timber

class LoginRepository @Inject constructor(
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
                    Timber.i("Result null")
                    Result.Failure(ViewError.NETWORK)
                }
                result.status == HttpStatusCode.OK -> {
                    Timber.i("Result Ok")
                    val token: Token? = result.receive()
                    token?.let {
                        Timber.i("Token Ok")
                        prefManager.setToken(token.authToken)
                        prefManager.setUserId(id)
                        prefManager.setKey(key)
                        Result.Success(true)
                    } ?: run {
                        Timber.i("Token Empty")
                        Result.Failure(ViewError.LOGIN_NO_TOKEN)
                    }
                }
                else -> {
                    Timber.i("Result not ok")
                    Result.Failure(ViewError.LOGIN_REQUEST_FAILED)
                }
            }
        }
    }
}