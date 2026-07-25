package com.rodzina.wyjazdy.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodzina.wyjazdy.di.AppContainer

@Composable
fun OnboardingScreen(container: AppContainer, userId: String, displayName: String) {
    val viewModel: OnboardingViewModel = viewModel(
        key = userId,
        factory = OnboardingViewModel.factory(userId, container.familyRepository, container.userRepository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    var inviteCode by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Witaj, $displayName!", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Dołącz do rodziny kodem zaproszenia albo stwórz nową.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        Text(text = "Mam kod zaproszenia", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it.uppercase() },
            label = { Text("Kod zaproszenia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        Button(
            onClick = { viewModel.joinFamily(inviteCode) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Dołącz do rodziny")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        Text(text = "Stwórz nową rodzinę", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = { Text("Nazwa rodziny") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        Button(
            onClick = { viewModel.createFamily(familyName) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Stwórz rodzinę")
        }

        when (val s = state) {
            OnboardingUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            is OnboardingUiState.Error -> Text(
                text = s.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
            OnboardingUiState.Idle -> Unit
        }
    }
}
