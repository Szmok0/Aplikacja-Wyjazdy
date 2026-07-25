package com.rodzina.wyjazdy.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rodzina.wyjazdy.util.formatDateTime
import com.rodzina.wyjazdy.util.toTimestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: LocalDateTime,
    onValueChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf(value.toLocalDate()) }

    OutlinedTextField(
        value = formatDateTime(value.toTimestamp()),
        onValueChange = {},
        readOnly = true,
        enabled = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {},
    )
    // Nakładka klikalna - OutlinedTextField w Compose nie ma prostego "onClick" bez focusable/interactionSource,
    // więc otwieramy dialogi z osobnego, przezroczystego przycisku poniżej pola dla prostoty MVP.
    TextButton(onClick = { showDatePicker = true }, modifier = modifier.fillMaxWidth()) {
        Text("Zmień: $label")
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        pendingDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Dalej") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = value.hour,
            initialMinute = value.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(LocalDateTime.of(pendingDate, LocalTime.of(timeState.hour, timeState.minute)))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") } },
            text = { TimePicker(state = timeState) },
        )
    }
}
