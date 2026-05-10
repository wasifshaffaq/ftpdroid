package com.ftpdroid.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ftpdroid.app.ui.screen.home.HomeScreen
import com.ftpdroid.app.ui.screen.server.ServerDashboardScreen
import com.ftpdroid.app.ui.screen.server.ServerSettingsScreen
import com.ftpdroid.app.ui.screen.server.UserManagerScreen
import com.ftpdroid.app.ui.screen.server.ConnectionLogScreen
import com.ftpdroid.app.ui.screen.client.ProfileListScreen
import com.ftpdroid.app.ui.screen.client.AddEditProfileScreen
import com.ftpdroid.app.ui.screen.client.FileBrowserScreen
import com.ftpdroid.app.ui.screen.transfer.TransferQueueScreen
import com.ftpdroid.app.ui.screen.transfer.TransferHistoryScreen
import com.ftpdroid.app.ui.screen.settings.AppSettingsScreen

private fun defaultEnterTransition(): EnterTransition = slideInHorizontally { it } + fadeIn()
private fun defaultExitTransition(): ExitTransition = slideOutHorizontally { -it } + fadeOut()
private fun defaultPopEnterTransition(): EnterTransition = slideInHorizontally { -it } + fadeIn()
private fun defaultPopExitTransition(): ExitTransition = slideOutHorizontally { it } + fadeOut()

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() }
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToServer = { navController.navigate(Routes.ServerDashboard.route) },
                onNavigateToClient = { navController.navigate(Routes.ProfileList.route) },
                onNavigateToTransfers = { navController.navigate(Routes.TransferQueue.route) },
                onNavigateToSettings = { navController.navigate(Routes.AppSettings.route) }
            )
        }

        composable(Routes.ServerDashboard.route) {
            ServerDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Routes.ServerSettings.route) },
                onNavigateToUsers = { navController.navigate(Routes.UserManager.route) },
                onNavigateToLogs = { navController.navigate(Routes.ConnectionLog.route) }
            )
        }

        composable(Routes.ServerSettings.route) {
            ServerSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.UserManager.route) {
            UserManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ConnectionLog.route) {
            ConnectionLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ProfileList.route) {
            ProfileListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProfile = { 
                    navController.navigate(Routes.createAddEditProfileRoute()) 
                },
                onNavigateToEditProfile = { profileId ->
                    navController.navigate(Routes.createAddEditProfileRoute(profileId))
                },
                onNavigateToFileBrowser = { profileId, path ->
                    navController.navigate(Routes.createFileBrowserRoute(profileId, path))
                }
            )
        }

        composable(
            route = Routes.AddEditProfile.route,
            arguments = listOf(
                navArgument(Routes.Companion.NavArgs.PROFILE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.FileBrowser.route,
            arguments = listOf(
                navArgument(Routes.Companion.NavArgs.PROFILE_ID) { type = NavType.LongType },
                navArgument(Routes.Companion.NavArgs.PATH) { type = NavType.StringType }
            )
        ) {
            FileBrowserScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TransferQueue.route) {
            TransferQueueScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Routes.TransferHistory.route) }
            )
        }

        composable(Routes.TransferHistory.route) {
            TransferHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AppSettings.route) {
            AppSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}