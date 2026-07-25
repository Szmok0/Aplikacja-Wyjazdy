package com.rodzina.wyjazdy.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.TransportType
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.util.formatDateRange

fun transportIcon(type: TransportType): ImageVector = when (type) {
    TransportType.CAR -> Icons.Filled.DirectionsCar
    TransportType.TRAIN -> Icons.Filled.Train
    TransportType.BUS -> Icons.Filled.DirectionsBus
    TransportType.PLANE -> Icons.Filled.Flight
    TransportType.OTHER -> Icons.Filled.MoreHoriz
}

@Composable
fun TripCard(
    trip: Trip,
    owner: User?,
    memberUserIds: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (owner != null) {
                AvatarBadge(user = owner, memberUserIds = memberUserIds)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = trip.city, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatDateRange(trip.dateStart, trip.dateEnd),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (trip.eventTitle.isNotBlank()) {
                    Text(
                        text = trip.eventTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = transportIcon(trip.transportType),
                contentDescription = trip.transportType.label,
                modifier = Modifier.size(20.dp),
            )
            StatusChip(status = trip.status)
        }
    }
}
