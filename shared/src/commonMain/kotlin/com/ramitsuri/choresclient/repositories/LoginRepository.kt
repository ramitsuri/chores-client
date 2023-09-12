package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.network.api.LoginApi

@Suppress("MoveVariableDeclarationIntoWhen")
class LoginRepository(
    private val api: LoginApi,
    private val prefManager: PrefManager,
) {
    suspend fun login(id: String, key: String): Result<Unit> {
        val result = api.login(id, key)
        return when (result) {
            is Result.Success -> {
                prefManager.setToken(result.data.authToken)
                prefManager.setLoggedInMemberId(id)
                prefManager.setKey(key)
                Result.Success(Unit)
            }

            is Result.Failure -> {
                result
            }
        }
    }
}