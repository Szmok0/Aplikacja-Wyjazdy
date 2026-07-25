package com.rodzina.wyjazdy.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rodzina.wyjazdy.data.model.TripStatus
import com.rodzina.wyjazdy.ui.theme.statusColor

@Composable
fun StatusChip(status: TripStatus, modifier: Modifier = Modifier) {
    Text(
        text = status.label,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium,
        color = androidx.compose.ui.graphics.Color.White,
        modifier = modifier
            .background(color = statusColor(status), shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
