package com.ramitsuri.choresclient.android.di

import com.ramitsuri.choresclient.android.network.TaskAssignmentsApi
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.ktor.client.HttpClient

@Module
@InstallIn(FragmentComponent::class)
class AssignmentsModule {
    @Provides
    fun provideAssignmentsApi(httpClient: HttpClient, baseUrl: String) =
        TaskAssignmentsApi(httpClient, baseUrl)

    @Provides
    fun provideAssignmentsRepository(
        api: TaskAssignmentsApi,
        dispatcherProvider: DispatcherProvider
    ) = TaskAssignmentsRepository(api, dispatcherProvider)
}