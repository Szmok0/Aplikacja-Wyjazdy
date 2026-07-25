package com.rodzina.wyjazdy.ui.navigation

object Destinations {
    const val TRIP_LIST = "tripList"
    const val CALENDAR = "calendar"
    const val MAP = "map"
    const val PEOPLE = "people"
    const val PROFILE = "profile"

    const val TRIP_EDIT_ARG = "tripId"
    const val TRIP_EDIT = "tripEdit?$TRIP_EDIT_ARG={$TRIP_EDIT_ARG}"
    fun tripEdit(tripId: String? = null) = "tripEdit?$TRIP_EDIT_ARG=${tripId.orEmpty()}"

    const val TRIP_DETAILS_ARG = "tripId"
    const val TRIP_DETAILS = "tripDetails/{$TRIP_DETAILS_ARG}"
    fun tripDetails(tripId: String) = "tripDetails/$tripId"

    const val ROUTE_MAP_ARG = "tripId"
    const val ROUTE_MAP = "routeMap/{$ROUTE_MAP_ARG}"
    fun routeMap(tripId: String) = "routeMap/$tripId"
}
