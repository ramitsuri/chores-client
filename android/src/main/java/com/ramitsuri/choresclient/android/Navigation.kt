package com.ramitsuri.choresclient.android

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.ramitsuri.choresclient.android.model.DeepLink
import com.ramitsuri.choresclient.android.ui.assigments.AssignmentsScreen
import com.ramitsuri.choresclient.android.ui.assigments.CompletedAssignmentsScreen
import com.ramitsuri.choresclient.android.ui.login.LoginScreen
import com.ramitsuri.choresclient.android.ui.settings.SettingsScreen
import com.ramitsuri.choresclient.android.ui.task.AddTaskScreen
import com.ramitsuri.choresclient.android.ui.task.EditTaskScreen
import com.ramitsuri.choresclient.android.ui.task.TasksScreen
import com.ramitsuri.choresclient.viewmodel.AddTaskViewModel
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.CompletedAssignmentsViewModel
import com.ramitsuri.choresclient.viewmodel.EditTaskViewModel
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import com.ramitsuri.choresclient.viewmodel.SettingsViewModel
import com.ramitsuri.choresclient.viewmodel.TasksViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private object Args {
    const val TASK_ID = "taskId"
    const val ASSIGNMENT_ID = "assignmentId"
}

object Destinations {
    const val LOGIN = "login"
    const val ASSIGNMENTS = "assignments"
    const val SETTINGS = "settings"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task"
    const val COMPLETED = "completed"
    const val TASKS = "tasks"
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
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
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

        composable(
            route = Destinations.ASSIGNMENTS,
            deepLinks = listOf(
                DeepLink.ASSIGNMENT.uriWithArgNames(),
                DeepLink.COMPLETED_BY_OTHERS.uriWithArgNames()
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString(Args.ASSIGNMENT_ID)
            val viewModel =
                koinViewModel<AssignmentsViewModel>(parameters = { parametersOf(assignmentId) })
            val state by viewModel.state.collectAsStateWithLifecycle()
            AssignmentsScreen(
                onSettingsClicked = { navController.navigate(Destinations.SETTINGS) },
                onCompletedClicked = { navController.navigate(Destinations.COMPLETED) },
                onTasksClicked = { navController.navigate(Destinations.TASKS) },
                onAddTaskClicked = { navController.navigate(Destinations.ADD_TASK) },
                onEditTaskClicked = {
                    navController.navigate("${Destinations.EDIT_TASK}/$it")
                },
                viewState = state,
                onMarkAsDone = viewModel::markAsDone,
                onMarkAsWontDo = viewModel::markAsWontDo,
                onUndoPendingAction = viewModel::undoPendingAction,
                onSnooze = viewModel::onSnooze,
                onTransferAssignmentClicked = viewModel::transferAssignment,
                onListTypeClicked = viewModel::onListTypeClicked,
            )
        }

        composable(Destinations.COMPLETED) {
            val viewModel = koinViewModel<CompletedAssignmentsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CompletedAssignmentsScreen(
                viewState = state,
                onBack = { navController.navigateUp() },
                onListTypeClicked = viewModel::onListTypeClicked,
            )
        }

        composable(Destinations.TASKS) {
            val viewModel = koinViewModel<TasksViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            TasksScreen(
                viewState = state,
                onBack = { navController.navigateUp() },
                onAddTaskClicked = { navController.navigate(Destinations.ADD_TASK) },
                onEditTaskClicked = { navController.navigate("${Destinations.EDIT_TASK}/$it") },
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onListTypeClicked = viewModel::onListTypeClicked,
            )
        }

        composable(Destinations.SETTINGS) {
            val viewModel = koinViewModel<SettingsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsScreen(
                onBack = { navController.navigateUp() },
                state = state,
                onSyncClicked = viewModel::syncRequested,
                onNotificationActionSelected = viewModel::onNotificationActionClicked,
                onNotificationActionsSaveRequested = viewModel::saveNotificationActions,
                onNotificationActionsResetRequested = viewModel::resetNotificationActions,
                onEnableRemoteLoggingClicked = viewModel::toggleLogging,
                onAllowTaskAssignmentUndoClicked = viewModel::toggleAllowTaskAssignmentUndo,
                onSwipeForTaskAssignmentCompletionClicked = viewModel::toggleSwipeForTaskAssignmentCompletion,
                onErrorAcknowledged = viewModel::onErrorShown,
                onEnableRemindPastDueClicked = viewModel::toggleRemindPastDue,
                onMorningTimePicked = viewModel::onMorningTimeSelected,
                onEveningTimePicked = viewModel::onEveningTimeSelected,
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
                onTaskDescriptionUpdate = viewModel::onTaskDescriptionUpdated,
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
            val taskId = backStackEntry.arguments?.getString(Args.TASK_ID) ?: ""
            val viewModel = koinViewModel<EditTaskViewModel>(parameters = { parametersOf(taskId) })

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
                onTaskDescriptionUpdate = viewModel::onTaskDescriptionUpdated,
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

fun DeepLink.uriWithArgsValues(args: List<String> = listOf()): String {
    return when (this) {
        DeepLink.ASSIGNMENT -> {
            uri.plus("/").plus(args.joinToString("/"))
        }

        DeepLink.COMPLETED_BY_OTHERS -> {
            uri
        }
    }
}

fun DeepLink.uriWithArgNames(): NavDeepLink {
    val pattern = when (this) {
        DeepLink.ASSIGNMENT -> {
            uri.plus("/").plus("{${Args.ASSIGNMENT_ID}}")
        }

        DeepLink.COMPLETED_BY_OTHERS -> {
            uri
        }
    }
    return navDeepLink {
        uriPattern = pattern
    }
}
