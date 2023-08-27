package com.ramitsuri.choresclient.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ramitsuri.choresclient.android.ui.assigments.AssignmentsScreen
import com.ramitsuri.choresclient.android.ui.login.LoginScreen
import com.ramitsuri.choresclient.android.ui.settings.SettingsScreen
import com.ramitsuri.choresclient.android.ui.task.AddEditTasksScreen
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import org.koin.androidx.compose.koinViewModel

private object Screens {
    const val LOGIN = "login"
    const val ASSIGNMENTS = "assignments"
    const val SETTINGS = "settings"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task"
}

object Destinations {
    const val LOGIN_ROUTE = Screens.LOGIN
    const val ASSIGNMENTS_ROUTE = Screens.ASSIGNMENTS
    const val SETTINGS_ROUTE = Screens.SETTINGS
    const val ADD_TASK_ROUTE = Screens.ADD_TASK
    const val EDIT_TASK_ROUTE = Screens.EDIT_TASK
}

class NavigationActions(private val navController: NavHostController) {
    fun navigateToAssignments(shouldRefreshFilter: Boolean) {
        navController.navigate("${Destinations.ASSIGNMENTS_ROUTE}/$shouldRefreshFilter") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToSettings() {
        navController.navigate(Destinations.SETTINGS_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToAddTask() {
        navController.navigate(Destinations.ADD_TASK_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToEditTask(taskId: String) {
        navController.navigate("${Destinations.EDIT_TASK_ROUTE}/$taskId") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.LOGIN_ROUTE,
    navActions: NavigationActions = remember(navController) {
        NavigationActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destinations.LOGIN_ROUTE) {
            val viewModel = koinViewModel<LoginViewModel>()
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
            "${Destinations.ASSIGNMENTS_ROUTE}/{refreshFilter}",
            arguments = listOf(navArgument("refreshFilter") { type = NavType.BoolType })
        ) { backStackEntry ->
            val shouldRefreshFilter = backStackEntry.arguments?.getBoolean("refreshFilter") ?: false
            AssignmentsScreen(
                shouldRefreshFilter,
                onSettingsClicked = { navActions.navigateToSettings() },
                onAddTaskClicked = { navActions.navigateToAddTask() },
                onEditTaskClicked = { navActions.navigateToEditTask(it) })
        }
        composable(Destinations.SETTINGS_ROUTE) {
            SettingsScreen(onBack = { navActions.navigateToAssignments(shouldRefreshFilter = true) })
        }
        composable(
            Destinations.ADD_TASK_ROUTE
        ) {
            AddEditTasksScreen(
                taskId = null,
                onBack = { navActions.navigateToAssignments(shouldRefreshFilter = false) })
        }
        composable(
            "${Destinations.EDIT_TASK_ROUTE}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddEditTasksScreen(
                taskId = backStackEntry.arguments?.getString("taskId"),
                onBack = { navActions.navigateToAssignments(shouldRefreshFilter = false) })
        }
    }
}