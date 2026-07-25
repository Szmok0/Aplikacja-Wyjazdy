package com.rodzina.wyjazdy.util

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private val zone: ZoneId = ZoneId.systemDefault()
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("pl"))
private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("pl"))
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("pl"))

fun Timestamp.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toDate().toInstant(), zone)

fun Timestamp.toLocalDate(): LocalDate = toLocalDateTime().toLocalDate()

fun LocalDateTime.toTimestamp(): Timestamp = Timestamp(Date.from(atZone(zone).toInstant()))

fun formatDate(ts: Timestamp): String = ts.toLocalDateTime().format(dateFormatter)

fun formatDateTime(ts: Timestamp): String = ts.toLocalDateTime().format(dateTimeFormatter)

fun formatTime(ts: Timestamp): String = ts.toLocalDateTime().format(timeFormatter)

fun formatDateRange(start: Timestamp, end: Timestamp): String {
    val startDate = start.toLocalDate()
    val endDate = end.toLocalDate()
    return if (startDate == endDate) {
        "${formatDate(start)}, ${formatTime(start)}–${formatTime(end)}"
    } else {
        "${formatDateTime(start)} – ${formatDateTime(end)}"
    }
}
