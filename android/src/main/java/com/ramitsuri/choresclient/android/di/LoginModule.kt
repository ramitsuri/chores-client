package com.ramitsuri.choresclient.android.di

import com.ramitsuri.choresclient.android.network.LoginApi
import com.ramitsuri.choresclient.android.repositories.LoginRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.ktor.client.*

@Module
@InstallIn(FragmentComponent::class)
class LoginModule {
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