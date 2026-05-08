package com.rosterforge.wh40k.presentation.leader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosterforge.wh40k.presentation.theme.ErrorRed

@Composable
fun LeaderAttachScreen(
    onBack: () -> Unit,
    viewModel: LeaderAttachViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attach Leader") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            LeaderAttachUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is LeaderAttachUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(s.message)
            }
            is LeaderAttachUiState.Success -> Content(s, padding, viewModel::onChooseCandidate, viewModel::onDetach)
        }
    }
}

@Composable
private fun Content(
    s: LeaderAttachUiState.Success,
    padding: PaddingValues,
    onChoose: (LeaderCandidate) -> Unit,
    onDetach: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = "Pairing for: ${s.sourceName}",
                style = MaterialTheme.typography.titleMedium,
            )
            if (s.currentLeaderRosterUnitId != null) {
                OutlinedButton(onClick = onDetach) { Text("Detach Current") }
            }
        }
        items(s.candidates, key = { it.rosterUnit.id }) { candidate ->
            Card(
                onClick = { if (candidate.eligible) onChoose(candidate) },
                elevation = CardDefaults.cardElevation(if (candidate.eligible) 1.dp else 0.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = candidate.rosterUnit.unitName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (candidate.eligible)
                            MaterialTheme.colorScheme.onSurface
                        else
                            ErrorRed,
                    )
                    val statusLine = when {
                        !candidate.eligible -> "Not eligible (keywords don't match)"
                        candidate.rosterUnit.attachedLeaderRosterUnitId != null ->
                            "Currently has another leader attached"
                        else -> "Eligible"
                    }
                    Text(
                        text = statusLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
