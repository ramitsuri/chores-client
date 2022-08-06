package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.House
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.SyncResult
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.network.SyncApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext

class SyncRepository(
    private val houseDataSource: HouseDataSource,
    private val syncApi: SyncApi,
    private val prefManager: PrefManager,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun refresh(): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            val result = try {
                syncApi.sync()
            } catch (e: Exception) {
                null
            }

            when (result?.status) {
                HttpStatusCode.OK -> {
                    val syncResult: SyncResult = result.body()
                    houseDataSource.saveHouses(syncResult.associatedLists)
                    prefManager.setLastSyncTime()
                    Result.Success(Unit)
                }
                else -> {
                    Result.Failure(ViewError.NETWORK)
                }
            }
        }
    }

    suspend fun getLocal(): List<House> {
        return houseDataSource.get()
    }

    companion object {
        private const val TAG = "SyncRepository"
    }
}