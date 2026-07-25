package com.rodzina.wyjazdy.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.di.AppContainer

private val HOUR_OPTIONS = listOf(72, 24, 3, 1)

@Composable
fun NotificationSettingsScreen(
    container: AppContainer,
    currentUser: User,
    onBack: () -> Unit,
) {
    val viewModel: NotificationSettingsViewModel = viewModel(
        key = currentUser.id,
        factory = NotificationSettingsViewModel.factory(currentUser.id, currentUser, container.userRepository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Powiadomienia") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Przypominaj o moich wyjazdach przed:", style = MaterialTheme.typography.titleSmall)
            HOUR_OPTIONS.forEach { hour ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = hour in state.reminderHoursBefore,
                        onCheckedChange = { viewModel.toggleHour(hour) },
                    )
                    Text(if (hour >= 24) "${hour / 24} dzień/dni wcześniej" else "$hour h wcześniej")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Switch(checked = state.statusUpdates, onCheckedChange = viewModel::setStatusUpdates)
                Text("Powiadomienia o zmianach statusu wyjazdów rodziny", modifier = Modifier.padding(start = 8.dp))
            }

            Button(onClick = viewModel::save, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.saved) "Zapisano" else "Zapisz")
            }
        }
    }
}
