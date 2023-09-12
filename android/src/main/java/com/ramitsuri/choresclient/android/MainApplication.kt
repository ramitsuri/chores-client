package com.ramitsuri.choresclient.android

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.ramitsuri.choresclient.AppInfo
import com.ramitsuri.choresclient.android.notification.DefaultNotificationManager
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.android.reminder.DefaultAlarmHandler
import com.ramitsuri.choresclient.android.work.ContentDownloadWorker
import com.ramitsuri.choresclient.android.work.PushMessageTokenUploader
import com.ramitsuri.choresclient.data.db.dao.AlarmDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.initKoin
import com.ramitsuri.choresclient.notification.Importance
import com.ramitsuri.choresclient.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.ContentDownloader
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.FilterHelper
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.viewmodel.AddTaskViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.EditTaskViewModel
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import com.ramitsuri.choresclient.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import java.util.UUID

class MainApplication : Application(), KoinComponent {

    private val notificationManager: NotificationManager by inject()
    private val prefManager: PrefManager by inject()
    private val logger: LogHelper by inject()

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        initDependencyInjection()
        logger.enableRemoteLogging(prefManager.getEnableRemoteLogging())

        createNotificationChannels()
        enqueueWorkers()
        PushMessageTokenUploader.upload(this)
        setDeviceIdIfNecessary()
    }

    private fun createNotificationChannels() {
        notificationManager.createChannels(
            listOf(
                NotificationChannelInfo(
                    id = getString(R.string.notification_reminders_id),
                    name = getString(R.string.notification_reminders_name),
                    description = getString(R.string.notification_reminders_description),
                    Importance.HIGH
                ),
                NotificationChannelInfo(
                    id = getString(R.string.notification_worker_id),
                    name = getString(R.string.notification_worker_name),
                    description = getString(R.string.notification_worker_description),
                    Importance.MIN
                )
            )
        )
    }

    private fun enqueueWorkers() {
        if (BuildConfig.DEBUG) {
            return
        }
        ContentDownloadWorker.enqueuePeriodic(this)
    }

    private fun initDependencyInjection() {
        initKoin(
            appModule = module {
                single<Context> {
                    this@MainApplication
                }

                single<NotificationManager> {
                    DefaultNotificationManager(get<Context>())
                }

                single<AlarmHandler> {
                    DefaultAlarmHandler(
                        ShowNotificationWorker.Companion,
                        get<NotificationManager>(),
                        get<AlarmDao>(),
                        get<Context>()
                    )
                }

                factory<AppInfo> {
                    AndroidAppInfo()
                }

                factory<ContentDownloadRequestHandler> {
                    ContentDownloadWorker
                }

                viewModel {
                    AssignmentsViewModel(
                        get<TaskAssignmentsRepository>(),
                        get<FilterHelper>(),
                        get<PrefManager>(),
                        get<CoroutineScope>()
                    )
                }

                viewModel {
                    LoginViewModel(
                        get<LoginRepository>(),
                        get<ContentDownloadRequestHandler>(),
                        get<PrefManager>(),
                        get<AppInfo>().isDebug
                    )
                }

                viewModel {
                    SettingsViewModel(
                        get<ContentDownloadRequestHandler>(),
                        get<FilterHelper>(),
                        get<PrefManager>(),
                        get<DispatcherProvider>(),
                        get<LogHelper>()
                    )
                }

                viewModel {
                    AddTaskViewModel(
                        get<TasksRepository>(),
                        get<SyncRepository>(),
                        get<DispatcherProvider>()
                    )
                }

                viewModel {
                    EditTaskViewModel(
                        get<TasksRepository>(),
                        get<ContentDownloader>(),
                        get<DispatcherProvider>(),
                        get<Clock>()
                    )
                }
            }
        )
    }

    private fun setDeviceIdIfNecessary() {
        val currentDeviceId = prefManager.getDeviceId()
        if (currentDeviceId != null) {
            return
        }

        val deviceId = UUID.randomUUID().toString()
        prefManager.setDeviceId(deviceId)
    }
}

class AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val deviceDetails: String = Build.MODEL
}
