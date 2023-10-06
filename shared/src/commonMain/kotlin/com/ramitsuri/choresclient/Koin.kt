package com.ramitsuri.choresclient

import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.db.dao.AlarmDao
import com.ramitsuri.choresclient.data.db.dao.HouseDao
import com.ramitsuri.choresclient.data.db.dao.MemberDao
import com.ramitsuri.choresclient.data.db.dao.MemberHouseAssociationDao
import com.ramitsuri.choresclient.data.db.dao.TaskAssignmentDao
import com.ramitsuri.choresclient.data.db.dao.TaskDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.network.NetworkProvider
import com.ramitsuri.choresclient.notification.CompletedByOthersNotificationHandler
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.reminder.ReminderScheduler
import com.ramitsuri.choresclient.repositories.DefaultTaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.DefaultTasksRepository
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.PushMessageTokenRepository
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.AppLifecycleObserver
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.ContentDownloader
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.FilterHelper
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.PushMessageProcessor
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
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
            get<AlarmHandler>(),
            get<PrefManager>(),
        )
    }

    single<CoroutineScope> {
        CoroutineScope(SupervisorJob())
    }

    single<ContentDownloader> {
        ContentDownloader(
            get<TaskAssignmentsRepository>(),
            get<SyncRepository>(),
            get<PrefManager>(),
            get<AppInfo>().isDebug,
        )
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

    factory<MemberHouseAssociationDao> {
        get<Database>().memberHouseAssociationDao
    }

    single<TaskAssignmentsRepository> {
        DefaultTaskAssignmentsRepository(
            get<NetworkProvider>().provideAssignmentsApi(),
            get<TaskAssignmentDao>(),
            get<MemberDao>(),
            get<TaskDao>(),
            get<ReminderScheduler>(),
            get<AlarmHandler>(),
            get<ContentDownloadRequestHandler>(),
        )
    }

    factory<LoginRepository> {
        get<NetworkProvider>().provideLoginRepository(
            prefManager = get<PrefManager>(),
        )
    }

    factory<SyncRepository> {
        SyncRepository(
            get<HouseDao>(),
            get<MemberHouseAssociationDao>(),
            get<MemberDao>(),
            get<NetworkProvider>().provideSyncApi(),
            get<PrefManager>(),
        )
    }

    factory<FilterHelper> {
        FilterHelper(
            get<SyncRepository>(),
            get<PrefManager>()
        )
    }

    factory<TasksRepository> {
        DefaultTasksRepository(
            get<NetworkProvider>().provideTasksApi(),
            get<TaskDao>(),
        )
    }

    factory<PushMessageTokenRepository> {
        PushMessageTokenRepository(
            get<PrefManager>(),
            get<NetworkProvider>().providePushMessageTokenApi(),
            get<DispatcherProvider>(),
            get<LogHelper>()
        )
    }

    single<Clock> {
        Clock.System
    }

    single<PushMessageProcessor> {
        PushMessageProcessor(
            get<CompletedByOthersNotificationHandler>(),
            get<ContentDownloadRequestHandler>(),
            get<AppLifecycleObserver>(),
        )
    }
}

expect val platformModule: Module

interface AppInfo {
    val appId: String
    val isDebug: Boolean
    val deviceDetails: String
}