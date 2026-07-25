package com.rodzina.wyjazdy.ui.theme

import androidx.compose.ui.graphics.Color

private val PersonPalette = listOf(
    Color(0xFF5E35B1),
    Color(0xFFD81B60),
    Color(0xFF00897B),
    Color(0xFFF9A825),
    Color(0xFF3949AB),
    Color(0xFF6D4C41),
)

/** Stały kolor per osoba (do awatara na liście i kropki w kalendarzu), wg pozycji uid na liście członków rodziny. */
fun personColor(userId: String, memberUserIds: List<String>): Color {
    val index = memberUserIds.indexOf(userId).let { if (it < 0) 0 else it }
    return PersonPalette[index % PersonPalette.size]
}

// Odcienie (hue 0-360) dla BitmapDescriptorFactory.defaultMarker() na mapie - Google Maps nie
// przyjmuje dowolnego RGB dla domyślnych pinezek, tylko hue, więc to przybliżenie PersonPalette.
private val PersonMarkerHues = listOf(262f, 330f, 174f, 45f, 231f, 20f)

fun personHueForMarker(userId: String, memberUserIds: List<String>): Float {
    val index = memberUserIds.indexOf(userId).let { if (it < 0) 0 else it }
    return PersonMarkerHues[index % PersonMarkerHues.size]
}
