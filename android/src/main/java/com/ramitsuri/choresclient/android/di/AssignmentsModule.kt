package com.ramitsuri.choresclient.android.di

import com.ramitsuri.choresclient.android.data.MemberDao
import com.ramitsuri.choresclient.android.data.TaskAssignmentDao
import com.ramitsuri.choresclient.android.data.TaskAssignmentDataSource
import com.ramitsuri.choresclient.android.data.TaskDao
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
    ) = TaskAssignmentsRepository(api, taskAssignmentDataSource, dispatcherProvider)
}