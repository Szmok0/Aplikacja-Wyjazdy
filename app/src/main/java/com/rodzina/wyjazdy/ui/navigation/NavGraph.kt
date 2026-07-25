package com.rodzina.wyjazdy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.SessionState
import com.rodzina.wyjazdy.ui.SessionViewModel
import com.rodzina.wyjazdy.ui.calendar.CalendarScreen
import com.rodzina.wyjazdy.ui.family.FamilySettingsScreen
import com.rodzina.wyjazdy.ui.map.RouteMapScreen
import com.rodzina.wyjazdy.ui.settings.NotificationSettingsScreen
import com.rodzina.wyjazdy.ui.tripdetails.TripDetailsScreen
import com.rodzina.wyjazdy.ui.tripedit.TripEditScreen
import com.rodzina.wyjazdy.ui.triplist.TripListScreen

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Destinations.TRIP_LIST, "Wyjazdy", Icons.Filled.List),
    BottomTab(Destinations.CALENDAR, "Kalendarz", Icons.Filled.CalendarMonth),
    BottomTab(Destinations.FAMILY, "Rodzina", Icons.Filled.Groups),
)

@Composable
fun MainNavHost(
    container: AppContainer,
    sessionViewModel: SessionViewModel,
    ready: SessionState.Ready,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.TRIP_LIST,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destinations.TRIP_LIST) {
                TripListScreen(
                    trips = ready.trips,
                    members = ready.members,
                    currentUserId = ready.currentUser.id,
                    onAddTrip = { navController.navigate(Destinations.tripEdit()) },
                    onOpenTrip = { tripId -> navController.navigate(Destinations.tripDetails(tripId)) },
                )
            }
            composable(Destinations.CALENDAR) {
                CalendarScreen(
                    trips = ready.trips,
                    members = ready.members,
                    onOpenTrip = { tripId -> navController.navigate(Destinations.tripDetails(tripId)) },
                )
            }
            composable(Destinations.FAMILY) {
                FamilySettingsScreen(
                    container = container,
                    family = ready.family,
                    members = ready.members,
                    currentUserId = ready.currentUser.id,
                    onOpenNotificationSettings = { navController.navigate(Destinations.NOTIFICATION_SETTINGS) },
                    onSignOut = onSignOut,
                )
            }
            composable(Destinations.NOTIFICATION_SETTINGS) {
                NotificationSettingsScreen(
                    container = container,
                    currentUser = ready.currentUser,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Destinations.TRIP_EDIT,
                arguments = listOf(navArgument(Destinations.TRIP_EDIT_ARG) { defaultValue = "" }),
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString(Destinations.TRIP_EDIT_ARG).orEmpty()
                val existingTrip = ready.trips.firstOrNull { it.id == tripId }
                TripEditScreen(
                    container = container,
                    familyId = ready.family.id,
                    currentUserId = ready.currentUser.id,
                    existingTrip = existingTrip,
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                route = Destinations.TRIP_DETAILS,
                arguments = listOf(navArgument(Destinations.TRIP_DETAILS_ARG) { defaultValue = "" }),
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString(Destinations.TRIP_DETAILS_ARG).orEmpty()
                val trip = ready.trips.firstOrNull { it.id == tripId }
                TripDetailsScreen(
                    container = container,
                    familyId = ready.family.id,
                    trip = trip,
                    members = ready.members,
                    currentUserId = ready.currentUser.id,
                    isAdmin = ready.family.adminUserId == ready.currentUser.id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Destinations.tripEdit(tripId)) },
                    onOpenRouteMap = { navController.navigate(Destinations.routeMap(tripId)) },
                )
            }
            composable(
                route = Destinations.ROUTE_MAP,
                arguments = listOf(navArgument(Destinations.ROUTE_MAP_ARG) { defaultValue = "" }),
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString(Destinations.ROUTE_MAP_ARG).orEmpty()
                val trip = ready.trips.firstOrNull { it.id == tripId }
                RouteMapScreen(trip = trip, onBack = { navController.popBackStack() })
            }
        }
    }
}
