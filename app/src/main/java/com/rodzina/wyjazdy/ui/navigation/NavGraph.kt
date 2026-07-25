package com.rodzina.wyjazdy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
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
import com.rodzina.wyjazdy.ui.map.MapScreen
import com.rodzina.wyjazdy.ui.map.RouteMapScreen
import com.rodzina.wyjazdy.ui.people.PeopleScreen
import com.rodzina.wyjazdy.ui.profile.ProfileScreen
import com.rodzina.wyjazdy.ui.tripdetails.TripDetailsScreen
import com.rodzina.wyjazdy.ui.tripedit.TripEditScreen
import com.rodzina.wyjazdy.ui.triplist.TripListScreen

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Destinations.TRIP_LIST, "Wyjazdy", Icons.Filled.List),
    BottomTab(Destinations.CALENDAR, "Kalendarz", Icons.Filled.CalendarMonth),
    BottomTab(Destinations.MAP, "Mapa", Icons.Filled.Map),
    BottomTab(Destinations.PEOPLE, "Osoby", Icons.Filled.People),
    BottomTab(Destinations.PROFILE, "Profil", Icons.Filled.Person),
)

@Composable
fun MainNavHost(
    container: AppContainer,
    sessionViewModel: SessionViewModel,
    ready: SessionState.Ready,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val memberUserIds = ready.members.map { it.id }
    val isAdmin = ready.family.adminUserId == ready.currentUser.id

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
                    container = container,
                    currentUserId = ready.currentUser.id,
                    trips = ready.trips,
                    members = ready.members,
                    onOpenTrip = { tripId -> navController.navigate(Destinations.tripDetails(tripId)) },
                )
            }
            composable(Destinations.MAP) {
                MapScreen(
                    trips = ready.trips,
                    members = ready.members,
                    onOpenTrip = { tripId -> navController.navigate(Destinations.tripDetails(tripId)) },
                )
            }
            composable(Destinations.PEOPLE) {
                PeopleScreen(
                    container = container,
                    family = ready.family,
                    members = ready.members,
                    currentUserId = ready.currentUser.id,
                )
            }
            composable(Destinations.PROFILE) {
                ProfileScreen(
                    container = container,
                    currentUser = ready.currentUser,
                    memberUserIds = memberUserIds,
                    onSignOut = onSignOut,
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
                    members = ready.members,
                    isAdmin = isAdmin,
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
                    isAdmin = isAdmin,
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
