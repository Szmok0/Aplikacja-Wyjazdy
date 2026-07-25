package com.rodzina.wyjazdy.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodzina.wyjazdy.data.model.Family
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.common.AvatarBadge
import com.rodzina.wyjazdy.ui.theme.personColor

@Composable
fun PeopleScreen(
    container: AppContainer,
    family: Family,
    members: List<User>,
    currentUserId: String,
) {
    val viewModel: PeopleViewModel = viewModel(
        key = family.id,
        factory = PeopleViewModel.factory(family.id, container.familyRepository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val memberUserIds = members.map { it.id }
    val isAdmin = family.adminUserId == currentUserId
    var memberPendingRemoval by remember { mutableStateOf<User?>(null) }
    var memberPendingTransfer by remember { mutableStateOf<User?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Osoby") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(family.name, style = MaterialTheme.typography.titleMedium)
                        Text("Kod zaproszenia", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = family.activeInviteCode ?: "—",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                enabled = family.activeInviteCode != null,
                                onClick = { clipboard.setText(AnnotatedString(family.activeInviteCode.orEmpty())) },
                            ) { Text("Kopiuj") }
                            if (isAdmin) {
                                OutlinedButton(onClick = { viewModel.regenerateInviteCode() }) { Text("Nowy kod") }
                            }
                        }
                    }
                }
            }

            item { Text("Członkowie", style = MaterialTheme.typography.titleMedium) }

            items(members, key = { it.id }) { member ->
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AvatarBadge(member, memberUserIds)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(personColor(member.id, memberUserIds), CircleShape),
                                )
                                Text(member.displayName.ifBlank { "Bez nazwy" })
                            }
                            Text(
                                text = if (member.id == family.adminUserId) "Admin" else "Członek rodziny",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (isAdmin && member.id != currentUserId) {
                            TextButton(onClick = { memberPendingTransfer = member }) { Text("Admin") }
                            TextButton(onClick = { memberPendingRemoval = member }) { Text("Usuń") }
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                item {
                    Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    memberPendingRemoval?.let { member ->
        AlertDialog(
            onDismissRequest = { memberPendingRemoval = null },
            title = { Text("Usunąć ${member.displayName} z rodziny?") },
            text = { Text("Osoba straci dostęp do wyjazdów rodziny.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeMember(member.id)
                    memberPendingRemoval = null
                }) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { memberPendingRemoval = null }) { Text("Anuluj") } },
        )
    }

    memberPendingTransfer?.let { member ->
        AlertDialog(
            onDismissRequest = { memberPendingTransfer = null },
            title = { Text("Przekazać rolę admina ${member.displayName}?") },
            text = { Text("Będziesz mógł/mogła stracić uprawnienia administratora.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.transferAdmin(member.id)
                    memberPendingTransfer = null
                }) { Text("Przekaż") }
            },
            dismissButton = { TextButton(onClick = { memberPendingTransfer = null }) { Text("Anuluj") } },
        )
    }
}
