package com.rodzina.wyjazdy.ui.theme

import androidx.compose.ui.graphics.Color
import com.rodzina.wyjazdy.data.model.TripStatus

/**
 * Kolory statusu wg sekcji 5 specyfikacji v2 ("Status koduje kolor"):
 * planowany - żółty/amber, w drodze - niebieski, dojechała - zielony, zakończony - szary.
 * "wraca" nie ma koloru w specyfikacji - dostaje odrębny odcień (fiolet), żeby odróżnić go od "w drodze".
 */
fun statusColor(status: TripStatus): Color = when (status) {
    TripStatus.PLANOWANY -> Color(0xFFF9A825)
    TripStatus.W_DRODZE -> Color(0xFF1565C0)
    TripStatus.DOJECHALA -> Color(0xFF2E7D32)
    TripStatus.WRACA -> Color(0xFF6A1B9A)
    TripStatus.ZAKONCZONY -> Color(0xFF9E9E9E)
}
