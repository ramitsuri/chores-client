package com.ramitsuri.choresclient.network

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.error.Error
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> apiRequest(
    ioDispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> HttpResponse
): Result<T> {
    return withContext(ioDispatcher) {
        var exception: Throwable? = null
        val response: HttpResponse? = try {
            call()
        } catch (e: Exception) {
            exception = e
            null
        }
        return@withContext when {
            response?.status == HttpStatusCode.OK -> {
                val data: T = response.body()
                Result.Success(data)
            }

            response?.status == HttpStatusCode.Created && T::class == Unit::class -> {
                Result.Success(response.body())
            }

            exception is ServerResponseException -> {
                Result.Failure(Error.Server(throwable = exception))
            }

            exception is IOException -> {
                Result.Failure(Error.NoInternet(exception))
            }

            exception is ClientRequestException ||
                    exception is SerializationException -> {
                Result.Failure(Error.Unknown(exception))
            }

            else -> {
                Result.Failure(Error.Unknown(exception))
            }
        }
    }
}