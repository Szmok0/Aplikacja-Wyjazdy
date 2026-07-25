package com.rodzina.wyjazdy.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.common.TripCard
import com.rodzina.wyjazdy.ui.theme.personColor
import com.rodzina.wyjazdy.util.toLocalDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val HOUR_OPTIONS = listOf(72, 24, 3, 1)

@Composable
fun CalendarScreen(
    container: AppContainer,
    currentUserId: String,
    trips: List<Trip>,
    members: List<User>,
    onOpenTrip: (String) -> Unit,
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = remember { daysOfWeek().first() }
    val today = remember { LocalDate.now() }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
    )

    val memberUserIds = members.map { it.id }
    val membersById = members.associateBy { it.id }
    val currentUser = membersById[currentUserId]

    val tripsByDate = remember(trips) {
        val map = mutableMapOf<LocalDate, MutableList<Trip>>()
        trips.forEach { trip ->
            var day = trip.dateStart.toLocalDate()
            val end = trip.dateEnd.toLocalDate()
            while (!day.isAfter(end)) {
                map.getOrPut(day) { mutableListOf() }.add(trip)
                day = day.plusDays(1)
            }
        }
        map
    }

    val now = remember { Timestamp.now() }
    val upcomingTrips = remember(trips) {
        trips.filter { it.dateEnd >= now }.sortedBy { it.dateStart.seconds }
    }

    var showReminderSettings by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Kalendarz") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item { PersonLegend(members, memberUserIds) }
            item {
                Column {
                    WeekHeader(firstDayOfWeek)
                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            CalendarDayCell(
                                day = day,
                                dayTrips = tripsByDate[day.date] ?: emptyList(),
                                memberUserIds = memberUserIds,
                                isToday = day.date == today,
                            )
                        },
                        monthHeader = { month -> MonthHeader(month) },
                    )
                    HorizontalDivider()
                }
            }

            item {
                Text(
                    "Nadchodzące wydarzenia",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
            if (upcomingTrips.isEmpty()) {
                item {
                    Text(
                        "Brak nadchodzących wyjazdów",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(upcomingTrips, key = { it.id }) { trip ->
                TripCard(
                    trip = trip,
                    owner = membersById[trip.ownerUserId],
                    memberUserIds = memberUserIds,
                    onClick = { onOpenTrip(trip.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (currentUser != null) {
                item {
                    ReminderSettingsSection(
                        container = container,
                        currentUserId = currentUserId,
                        initialHours = currentUser.notificationPrefs.reminderHoursBefore,
                        expanded = showReminderSettings,
                        onToggleExpanded = { showReminderSettings = !showReminderSettings },
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonLegend(members: List<User>, memberUserIds: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        members.forEach { member ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Dot(personColor(member.id, memberUserIds))
                Text(member.displayName.ifBlank { "?" }, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ReminderSettingsSection(
    container: AppContainer,
    currentUserId: String,
    initialHours: List<Int>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val viewModel: CalendarReminderViewModel = viewModel(
        key = currentUserId,
        factory = CalendarReminderViewModel.factory(currentUserId, initialHours, container.userRepository),
    )
    val selectedHours by viewModel.reminderHoursBefore.collectAsStateWithLifecycle()

    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Ustawienia przypomnień", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onToggleExpanded) {
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                }
            }
            if (expanded) {
                Text(
                    "Przypominaj o moich wyjazdach przed:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
                HOUR_OPTIONS.forEach { hour ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = hour in selectedHours, onCheckedChange = { viewModel.toggleHour(hour) })
                        Text(if (hour >= 24) "${hour / 24} dzień/dni wcześniej" else "$hour h wcześniej")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHeader(firstDayOfWeek: java.time.DayOfWeek) {
    val days = remember(firstDayOfWeek) {
        (0..6).map { firstDayOfWeek.plus(it.toLong()) }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        days.forEach { dow ->
            Text(
                text = dow.getDisplayName(TextStyle.SHORT, Locale("pl")).take(2).uppercase(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MonthHeader(month: CalendarMonth) {
    Text(
        text = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pl")) + " " + month.yearMonth.year,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    dayTrips: List<Trip>,
    memberUserIds: List<String>,
    isToday: Boolean,
) {
    val ownerColors = remember(dayTrips, memberUserIds) {
        dayTrips.map { it.ownerUserId }.distinct().take(3).map { personColor(it, memberUserIds) }
    }
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(
                if (isToday) Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                else Modifier,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            ownerColors.forEach { color -> Dot(color) }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(modifier = Modifier.size(5.dp).background(color = color, shape = CircleShape))
}
