package com.rodzina.wyjazdy.ui.theme

import androidx.compose.ui.graphics.Color
import com.rodzina.wyjazdy.data.model.TripStatus

/**
 * Kolory statusu wg sekcji 5 specyfikacji ("Status koduje kolor"):
 * planowany - szary/niebieski, w drodze - pomarańczowy, dojechała - zielony, zakończony - szary.
 * "wraca" nie ma koloru w specyfikacji - dostaje odrębny odcień niebieskiego, żeby odróżnić go od "planowany".
 */
fun statusColor(status: TripStatus): Color = when (status) {
    TripStatus.PLANOWANY -> Color(0xFF607D8B)
    TripStatus.W_DRODZE -> Color(0xFFEF6C00)
    TripStatus.DOJECHALA -> Color(0xFF2E7D32)
    TripStatus.WRACA -> Color(0xFF1565C0)
    TripStatus.ZAKONCZONY -> Color(0xFF9E9E9E)
}
