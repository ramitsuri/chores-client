package com.ramitsuri.choresclient.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ramitsuri.choresclient.android.ui.assigments.AssignmentsScreen
import com.ramitsuri.choresclient.android.ui.login.LoginScreen
import com.ramitsuri.choresclient.android.ui.settings.SettingsScreen
import com.ramitsuri.choresclient.android.ui.task.AddTaskScreen
import com.ramitsuri.choresclient.android.ui.task.EditTaskScreen
import com.ramitsuri.choresclient.viewmodel.AddTaskViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.EditTaskViewModel
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import com.ramitsuri.choresclient.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

private object Args {
    const val TASK_ID = "taskId"
}

object Destinations {
    const val LOGIN = "login"
    const val ASSIGNMENTS = "assignments"
    const val SETTINGS = "settings"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task"
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.LOGIN,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destinations.LOGIN) {
            val viewModel = koinViewModel<LoginViewModel>()
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    navController.navigate(Destinations.ASSIGNMENTS) {
                        popUpTo(Destinations.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            }

            val state by viewModel.state.collectAsStateWithLifecycle()
            LoginScreen(
                state = state,
                onIdEntered = viewModel::onIdUpdated,
                onKeyEntered = viewModel::onKeyUpdated,
                onLoginClick = viewModel::login,
                onErrorAcknowledged = viewModel::onErrorShown,
                onServerEntered = viewModel::onServerUrlUpdated,
                onServerSet = viewModel::setDebugServer,
                onResetServer = viewModel::resetDebugServer
            )
        }

        composable(Destinations.ASSIGNMENTS) {
            val viewModel = koinViewModel<AssignmentsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            AssignmentsScreen(
                onSettingsClicked = { navController.navigate(Destinations.SETTINGS) },
                onAddTaskClicked = { navController.navigate(Destinations.ADD_TASK) },
                onEditTaskClicked = {
                    navController.navigate("${Destinations.EDIT_TASK}/$it")
                },
                viewState = state,
                onMarkAsDone = viewModel::markAsDone,
                onMarkAsWontDo = viewModel::markAsWontDo,
                onSnooze = viewModel::onSnooze,
                onFilterItemClicked = viewModel::onFilterItemClicked,
                onCustomHoursEntered = viewModel::onCustomSnoozeHoursEntered,
                onCustomMinutesEntered = viewModel::onCustomSnoozeMinutesEntered,
                onCustomTimeSet = viewModel::onCustomSnoozeSet,
                onCustomTimeCanceled = viewModel::onCustomSnoozeCanceled,
                onItemClicked = viewModel::onItemClicked,
            )
        }

        composable(Destinations.SETTINGS) {
            val viewModel = koinViewModel<SettingsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsScreen(
                onBack = { navController.navigateUp() },
                state = state,
                onSyncClicked = viewModel::syncRequested,
                onFilterSelected = viewModel::filter,
                onFilterSaveRequested = viewModel::saveFilters,
                onFilterResetRequested = viewModel::resetFilters,
                onNotificationActionSelected = viewModel::onNotificationActionClicked,
                onNotificationActionsSaveRequested = viewModel::saveNotificationActions,
                onNotificationActionsResetRequested = viewModel::resetNotificationActions,
                onEnableRemoteLoggingClicked = viewModel::toggleLogging,
                onErrorAcknowledged = viewModel::onErrorShown,
                onEnableRemindPastDueClicked = viewModel::toggleRemindPastDue,
            )
        }

        composable(Destinations.ADD_TASK) {
            val viewModel = koinViewModel<AddTaskViewModel>()

            val taskAdded by viewModel.taskAdded.collectAsStateWithLifecycle()
            LaunchedEffect(taskAdded) {
                if (taskAdded) {
                    navController.navigateUp()
                }
            }

            val state by viewModel.state.collectAsStateWithLifecycle()
            AddTaskScreen(
                onBack = { navController.navigateUp() },
                viewState = state,
                onTaskNameUpdate = viewModel::onTaskNameUpdated,
                onHouseItemSelected = { viewModel.onHouseSelected(it.getId()) },
                onMemberItemSelected = { viewModel.onMemberSelected(it.getId()) },
                onDatePicked = viewModel::onDatePicked,
                onTimePicked = viewModel::onTimePicked,
                onRepeatValueUpdated = viewModel::onRepeatValueUpdated,
                onRepeatUnitSelected = { viewModel.onRepeatUnitSelected(it.getId()) },
                onRotateMemberClicked = viewModel::onRotateMemberUpdated,
                onRepeatEndDatePicked = viewModel::onRepeatEndDatePicked,
                onResetRepeatInfo = viewModel::onResetRepeatInfo,
                onAddTaskRequested = viewModel::addTaskRequested
            )
        }

        composable(
            "${Destinations.EDIT_TASK}/{${Args.TASK_ID}}",
            arguments = listOf(navArgument(Args.TASK_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel = koinViewModel<EditTaskViewModel>()
            val taskId = backStackEntry.arguments?.getString(Args.TASK_ID)
            LaunchedEffect(taskId) {
                if (taskId != null) {
                    viewModel.setTaskId(taskId)
                }
            }

            val taskEdited by viewModel.taskEdited.collectAsStateWithLifecycle()
            LaunchedEffect(taskEdited) {
                if (taskEdited) {
                    navController.navigateUp()
                }
            }

            val state by viewModel.state.collectAsStateWithLifecycle()
            EditTaskScreen(
                onBack = { navController.navigateUp() },
                viewState = state,
                onTaskNameUpdate = viewModel::onTaskNameUpdated,
                onDatePicked = viewModel::onDatePicked,
                onTimePicked = viewModel::onTimePicked,
                onRepeatValueUpdated = viewModel::onRepeatValueUpdated,
                onRepeatUnitSelected = { viewModel.onRepeatUnitSelected(it.getId()) },
                onRotateMemberClicked = viewModel::onRotateMemberUpdated,
                onRepeatEndDatePicked = viewModel::onRepeatEndDatePicked,
                onResetRepeatInfo = viewModel::onResetRepeatInfo,
                onEditTaskRequested = viewModel::editTaskRequested,
                onActiveStatusSelected = viewModel::onActiveStatusSelected,
                onResetActiveStatus = viewModel::onResetActiveStatus,
            )
        }
    }
}