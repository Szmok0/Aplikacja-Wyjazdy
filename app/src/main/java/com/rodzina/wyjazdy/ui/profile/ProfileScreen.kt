package com.rodzina.wyjazdy.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.common.AvatarBadge

@Composable
fun ProfileScreen(
    container: AppContainer,
    currentUser: User,
    memberUserIds: List<String>,
    onSignOut: () -> Unit,
) {
    val viewModel: ProfileViewModel = viewModel(
        key = currentUser.id,
        factory = ProfileViewModel.factory(currentUser.id, container.userRepository),
    )
    val email = container.authRepository.currentUser()?.email.orEmpty()
    var statusUpdatesEnabled by remember(currentUser.id) {
        mutableStateOf(currentUser.notificationPrefs.statusUpdates)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Profil") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarBadge(user = currentUser, memberUserIds = memberUserIds, size = 56.dp)
                Column {
                    Text(currentUser.displayName.ifBlank { "Bez nazwy" }, style = MaterialTheme.typography.titleLarge)
                    if (email.isNotBlank()) {
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Powiadomienia push", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "O zmianach statusu wyjazdów rodziny",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = statusUpdatesEnabled,
                        onCheckedChange = {
                            statusUpdatesEnabled = it
                            viewModel.setStatusUpdatesEnabled(it)
                        },
                    )
                }
            }

            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Wyloguj się")
            }
        }
    }
}
