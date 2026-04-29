package com.example.y.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.y.ui.screen.bills.BillsScreen
import com.example.y.ui.screen.settings.AccountManagementScreen
import com.example.y.ui.screen.settings.BackgroundSettingsScreen
import com.example.y.ui.screen.settings.CategoryManagementScreen
import com.example.y.ui.screen.settings.SettingsScreen
import com.example.y.ui.screen.stats.StatsScreen
import kotlin.math.roundToInt

/** 二级页面路由常量 */
object Routes {
    const val BACKGROUND_SETTINGS = "background_settings"
    const val ACCOUNT_MANAGEMENT = "account_management"
    const val CATEGORY_MANAGEMENT = "category_management"
}

private val TOP_ROUTES = TopDestination.entries.map { it.route }.toSet()
private val SECONDARY_ROUTES = setOf(
    Routes.BACKGROUND_SETTINGS,
    Routes.ACCOUNT_MANAGEMENT,
    Routes.CATEGORY_MANAGEMENT,
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    onShowAddSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopDestination.BILLS.route,
        modifier = modifier,
        enterTransition = { appEnterTransition() },
        exitTransition = { appExitTransition() },
        popEnterTransition = { appPopEnterTransition() },
        popExitTransition = { appPopExitTransition() },
    ) {
        composable(TopDestination.BILLS.route) {
            BillsScreen(onAddClick = onShowAddSheet)
        }
        composable(TopDestination.STATS.route) {
            StatsScreen()
        }
        composable(TopDestination.SETTINGS.route) {
            SettingsScreen(
                onNavigateToBackground = {
                    navController.navigate(Routes.BACKGROUND_SETTINGS)
                },
                onNavigateToAccountManagement = {
                    navController.navigate(Routes.ACCOUNT_MANAGEMENT)
                },
                onNavigateToCategoryManagement = {
                    navController.navigate(Routes.CATEGORY_MANAGEMENT)
                },
            )
        }
        composable(Routes.BACKGROUND_SETTINGS) {
            BackgroundSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.ACCOUNT_MANAGEMENT) {
            AccountManagementScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.CATEGORY_MANAGEMENT) {
            CategoryManagementScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appEnterTransition(): EnterTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return when {
        fromRoute.isTopRoute() && toRoute.isTopRoute() -> {
            val forward = toRoute.topRouteIndex() >= fromRoute.topRouteIndex()
            fadeIn(animationSpec = tween(durationMillis = 240, delayMillis = 40)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth ->
                        val ratio = if (forward) 0.22f else -0.22f
                        (fullWidth * ratio).roundToInt()
                    },
                )
        }

        fromRoute.isTopRoute() && toRoute.isSecondaryRoute() -> {
            fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 30)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.28f).roundToInt() },
                )
        }

        fromRoute.isSecondaryRoute() && toRoute.isTopRoute() -> {
            fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 20)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> -(fullWidth * 0.18f).roundToInt() },
                )
        }

        else -> fadeIn(animationSpec = tween(durationMillis = 180))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appExitTransition(): ExitTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return when {
        fromRoute.isTopRoute() && toRoute.isTopRoute() -> {
            val forward = toRoute.topRouteIndex() >= fromRoute.topRouteIndex()
            fadeOut(animationSpec = tween(durationMillis = 220)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth ->
                        val ratio = if (forward) -0.16f else 0.16f
                        (fullWidth * ratio).roundToInt()
                    },
                )
        }

        fromRoute.isTopRoute() && toRoute.isSecondaryRoute() -> {
            fadeOut(animationSpec = tween(durationMillis = 200)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> -(fullWidth * 0.12f).roundToInt() },
                )
        }

        fromRoute.isSecondaryRoute() && toRoute.isTopRoute() -> {
            fadeOut(animationSpec = tween(durationMillis = 180)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> (fullWidth * 0.12f).roundToInt() },
                )
        }

        else -> fadeOut(animationSpec = tween(durationMillis = 160))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopEnterTransition(): EnterTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return when {
        fromRoute.isSecondaryRoute() && toRoute.isTopRoute() -> {
            fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 20)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> -(fullWidth * 0.2f).roundToInt() },
                )
        }

        fromRoute.isTopRoute() && toRoute.isTopRoute() -> {
            val backward = toRoute.topRouteIndex() <= fromRoute.topRouteIndex()
            fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 20)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth ->
                        val ratio = if (backward) -0.2f else 0.2f
                        (fullWidth * ratio).roundToInt()
                    },
                )
        }

        else -> fadeIn(animationSpec = tween(durationMillis = 180))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopExitTransition(): ExitTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return when {
        fromRoute.isSecondaryRoute() && toRoute.isTopRoute() -> {
            fadeOut(animationSpec = tween(durationMillis = 180)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> (fullWidth * 0.24f).roundToInt() },
                )
        }

        fromRoute.isTopRoute() && toRoute.isTopRoute() -> {
            val backward = toRoute.topRouteIndex() <= fromRoute.topRouteIndex()
            fadeOut(animationSpec = tween(durationMillis = 180)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth ->
                        val ratio = if (backward) 0.14f else -0.14f
                        (fullWidth * ratio).roundToInt()
                    },
                )
        }

        else -> fadeOut(animationSpec = tween(durationMillis = 160))
    }
}

private fun String?.isTopRoute(): Boolean = this != null && this in TOP_ROUTES

private fun String?.isSecondaryRoute(): Boolean = this != null && this in SECONDARY_ROUTES

private fun String?.topRouteIndex(): Int {
    val route = this ?: return -1
    return TopDestination.entries.indexOfFirst { it.route == route }
}
