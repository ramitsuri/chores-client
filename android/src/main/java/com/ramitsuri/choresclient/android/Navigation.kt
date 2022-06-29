package com.ramitsuri.choresclient.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.choresclient.android.ui.assigments.AssignmentsScreen
import com.ramitsuri.choresclient.android.ui.login.LoginScreen

private object Screens {
    const val LOGIN = "login"
    const val ASSIGNMENTS = "assignments"
}

object Destinations {
    const val LOGIN_ROUTE = Screens.LOGIN
    const val ASSIGNMENTS_ROUTE = Screens.ASSIGNMENTS
}

class NavigationActions(private val navController: NavHostController) {
    fun navigateToAssignments() {
        navController.navigate(Destinations.ASSIGNMENTS_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
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
            LoginScreen(onLoggedIn = { navActions.navigateToAssignments() })
        }
        composable(Destinations.ASSIGNMENTS_ROUTE) {
            AssignmentsScreen()
        }
    }
}