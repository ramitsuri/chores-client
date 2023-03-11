package com.ramitsuri.choresclient.android

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.ramitsuri.choresclient.AppInfo
import com.ramitsuri.choresclient.android.work.AssignmentsDownloader
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.android.notification.SystemNotificationHandler
import com.ramitsuri.choresclient.android.reminder.ReminderSchedulerWorker
import com.ramitsuri.choresclient.android.reminder.SystemAlarmHandler
import com.ramitsuri.choresclient.android.work.PushMessageTokenUploader
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.initKoin
import com.ramitsuri.choresclient.notification.Importance
import com.ramitsuri.choresclient.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.PushMessageTokenRepository
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.FilterHelper
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.viewmodel.AddEditTaskViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import com.ramitsuri.choresclient.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import java.util.UUID

class MainApplication : Application(), KoinComponent {

    private val notificationHandler: NotificationHandler by inject()
    private val prefManager: PrefManager by inject()
    private val pushMessageTokenUploader: PushMessageTokenUploader.Companion by inject()
    private val logger: LogHelper by inject()

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        initDependencyInjection()
        logger.enableRemoteLogging(prefManager.getEnableRemoteLogging())

        createNotificationChannels()
        enqueueWorkers()
        pushMessageTokenUploader.upload(this)
        setDeviceIdIfNecessary()
    }

    private fun createNotificationChannels() {
        notificationHandler.createChannels(
            listOf(
                NotificationChannelInfo(
                    id = getString(R.string.notification_reminders_id),
                    name = getString(R.string.notification_reminders_name),
                    description = getString(R.string.notification_reminders_description),
                    Importance.HIGH
                )
            )
        )
    }

    private fun enqueueWorkers() {
        if (BuildConfig.DEBUG) {
            return
        }
        AssignmentsDownloader.enqueuePeriodic(this)
        ReminderSchedulerWorker.enqueuePeriodic(this)
    }

    private fun initDependencyInjection() {
        initKoin(
            appModule = module {
                single<Context> {
                    this@MainApplication
                }

                single<NotificationHandler> {
                    SystemNotificationHandler(get<Context>())
                }

                single<AlarmHandler> {
                    SystemAlarmHandler(
                        ShowNotificationWorker.Companion,
                        get<AlarmDao>(),
                        get<Context>()
                    )
                }

                factory<AppInfo> {
                    AndroidAppInfo()
                }

                factory<PushMessageTokenUploader.Companion> { PushMessageTokenUploader.Companion }

                viewModel {
                    AssignmentsViewModel(
                        get<AssignmentDetailsRepository>(),
                        get<TaskAssignmentsRepository>(),
                        get<FilterHelper>(),
                        get<AppHelper>(),
                        get<DispatcherProvider>(),
                        get<CoroutineScope>()
                    )
                }

                viewModel {
                    LoginViewModel(
                        get<LoginRepository>(),
                        get<SyncRepository>(),
                        get<TaskAssignmentsRepository>(),
                        get<PushMessageTokenRepository>(),
                        get<PrefManager>(),
                        get<DispatcherProvider>(),
                        BuildConfig.DEBUG
                    )
                }

                viewModel {
                    AssignmentDetailsViewModel(
                        get()
                    )
                }

                viewModel {
                    SettingsViewModel(
                        get<SyncRepository>(),
                        get<FilterHelper>(),
                        get<PrefManager>(),
                        get<DispatcherProvider>(),
                        get<LogHelper>()
                    )
                }

                viewModel {
                    AddEditTaskViewModel(
                        get<TasksRepository>(),
                        get<TaskAssignmentsRepository>(),
                        get<TaskDao>(),
                        get<SyncRepository>(),
                        get<DispatcherProvider>()
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
