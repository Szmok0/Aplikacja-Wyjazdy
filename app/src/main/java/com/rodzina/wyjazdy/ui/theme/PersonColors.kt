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
