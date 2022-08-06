package com.ramitsuri.choresclient

import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.entities.HouseDao
import com.ramitsuri.choresclient.data.entities.MemberDao
import com.ramitsuri.choresclient.data.entities.TaskAssignmentDao
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.data.notification.ReminderScheduler
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.network.NetworkProvider
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.HouseDataSource
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.SystemTaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentDataSource
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication {
    val koinApplication = startKoin {
        modules(
            appModule,
            platformModule,
            coreModule
        )
    }
    return koinApplication
}

private val coreModule = module {
    single<NetworkProvider> {
        NetworkProvider(
            get<PrefManager>(),
            get<AppInfo>().isDebug,
            get<HttpClientEngine>(),
            get<DispatcherProvider>()
        )
    }

    single<ReminderScheduler> {
        ReminderScheduler(
            get<TaskAssignmentsRepository>(),
            get<AlarmHandler>(),
            get<PrefManager>(),
            get<DispatcherProvider>()
        )
    }

    single<CoroutineScope> {
        CoroutineScope(SupervisorJob())
    }

    factory<TaskAssignmentDao> {
        get<Database>().taskAssignmentDao
    }

    factory<TaskDao> {
        get<Database>().taskDao
    }

    factory<MemberDao> {
        get<Database>().memberDao
    }

    factory<AlarmDao> {
        get<Database>().alarmDao
    }

    factory<HouseDao> {
        get<Database>().houseDao
    }

    factory<TaskAssignmentDataSource> {
        TaskAssignmentDataSource(
            get<TaskAssignmentDao>(),
            get<MemberDao>(),
            get<TaskDao>()
        )
    }

    factory<HouseDataSource> {
        HouseDataSource(get<HouseDao>())
    }

    factory<TaskAssignmentsRepository> {
        SystemTaskAssignmentsRepository(
            get<NetworkProvider>().provideAssignmentsApi(),
            get<TaskAssignmentDataSource>(),
            get<DispatcherProvider>()
        )
    }

    factory<AssignmentDetailsRepository> {
        AssignmentDetailsRepository(
            get<CoroutineScope>(),
            get<DispatcherProvider>(),
            get<TaskAssignmentsRepository>(),
            get<AlarmHandler>(),
            get<NotificationHandler>()
        )
    }

    factory<LoginRepository> {
        get<NetworkProvider>().provideLoginRepository(
            prefManager = get<PrefManager>(),
            dispatcherProvider = get<DispatcherProvider>()
        )
    }

    factory<SyncRepository> {
        SyncRepository(
            get<HouseDataSource>(),
            get<NetworkProvider>().provideSyncApi(),
            get<PrefManager>(),
            get<DispatcherProvider>()
        )
    }
}

expect val platformModule: Module

interface AppInfo {
    val appId: String
    val isDebug: Boolean
    val deviceDetails: String
}