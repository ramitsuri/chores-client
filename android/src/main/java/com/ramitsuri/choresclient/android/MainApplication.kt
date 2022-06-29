package com.ramitsuri.choresclient.android

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.ramitsuri.choresclient.AppInfo
import com.ramitsuri.choresclient.android.downloader.AssignmentsDownloader
import com.ramitsuri.choresclient.android.notification.ShowNotificationWorker
import com.ramitsuri.choresclient.android.notification.SystemNotificationHandler
import com.ramitsuri.choresclient.android.reminder.ReminderSchedulerWorker
import com.ramitsuri.choresclient.android.reminder.SystemAlarmHandler
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.initKoin
import com.ramitsuri.choresclient.notification.Importance
import com.ramitsuri.choresclient.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.LoginRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

class MainApplication : Application(), KoinComponent {

    private val notificationHandler: NotificationHandler by inject()

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        initDependencyInjection()

        createNotificationChannels()
        enqueueWorkers()
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

                viewModel {
                    AssignmentsViewModel(
                        get<AssignmentDetailsRepository>(),
                        get<TaskAssignmentsRepository>(),
                        get<PrefManager>(),
                        get<AppHelper>(),
                        get<DispatcherProvider>(),
                        get<CoroutineScope>()
                    )
                }

                viewModel {
                    LoginViewModel(
                        get<LoginRepository>(),
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
            }
        )
    }
}

class AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val deviceDetails: String = Build.MODEL
}