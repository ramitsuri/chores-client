package com.ramitsuri.choresclient

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.data.settings.SettingsKeyValueStore
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsCallbackViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsCallbackViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.LoginCallbackViewModel
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

fun initKoinIos(
    appInfo: AppInfo
): KoinApplication = initKoin(
    module {
        factory<AppInfo> {
            appInfo
        }

        factory<AssignmentsCallbackViewModel> {
            AssignmentsCallbackViewModel(
                AssignmentsViewModel(
                    get<AssignmentDetailsRepository>(),
                    get<TaskAssignmentsRepository>(),
                    get<PrefManager>(),
                    get<AppHelper>(),
                    get<DispatcherProvider>(),
                    get<CoroutineScope>()
                )
            )
        }

        factory<AssignmentDetailsCallbackViewModel> {
            AssignmentDetailsCallbackViewModel(
                AssignmentDetailsViewModel(
                    get()
                )
            )
        }

        factory<LoginCallbackViewModel> {
            LoginCallbackViewModel(
                LoginViewModel(
                    get<LoginRepository>(),
                    get<PrefManager>(),
                    get<DispatcherProvider>()
                )
            )
        }
    }
)

@Suppress("unused") // Called from Swift
object KotlinDependencies : KoinComponent {
    fun getAssignmentsViewModel() = getKoin().get<AssignmentsCallbackViewModel>()
    fun getAssignmentDetailsViewModel() = getKoin().get<AssignmentDetailsCallbackViewModel>()
    fun getLoginViewModel() = getKoin().get<LoginCallbackViewModel>()
}

@OptIn(ExperimentalSettingsImplementation::class)
actual val platformModule = module {
    factory<HttpClientEngine> {
        Darwin.create()
    }

    single<DispatcherProvider> {
        DispatcherProvider()
    }

    single<Database> {
        Database(
            NativeSqliteDriver(ChoresDatabase.Schema, "chores.db"),
            get<DispatcherProvider>()
        )
    }

    single<PrefManager> {
        val keyValueStore =
            SettingsKeyValueStore(AppleSettings(NSUserDefaults.standardUserDefaults))
        val secureKeyValueStore = SettingsKeyValueStore(KeychainSettings())
        PrefManager(keyValueStore, secureKeyValueStore)
    }

    single<AppHelper> {
        AppHelper()
    }
}