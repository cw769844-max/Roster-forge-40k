package com.rosterforge.wh40k.presentation.stratagems

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosterforge.wh40k.domain.model.GamePhase
import com.rosterforge.wh40k.domain.model.Stratagem

@Composable
fun StratagemReferenceScreen(
    onBack: () -> Unit,
    viewModel: StratagemReferenceViewModel = hiltViewModel(),
) {
    val list by viewModel.state.collectAsStateWithLifecycle()
    val q by viewModel.query.collectAsStateWithLifecycle()
    val phase by viewModel.phaseFilter.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stratagems") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            OutlinedTextField(
                value = q,
                onValueChange = viewModel::onQueryChange,
                singleLine = true,
                placeholder = { Text("Search stratagems") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = phase == null,
                    onClick = { viewModel.onPhaseChange(null) },
                    label = { Text("All") },
                )
                GamePhase.entries.forEach { p ->
                    FilterChip(
                        selected = phase == p,
                        onClick = { viewModel.onPhaseChange(p) },
                        label = { Text(p.name) },
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(list, key = { it.id }) { strat -> StratagemCard(strat) }
            }
        }
    }
}

@Composable
private fun StratagemCard(s: Stratagem) {
    Card(elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(s.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
                Text("${s.cp} CP",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                text = "${s.type.name} • ${s.phase.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (s.target.isNotBlank()) {
                Text(
                    text = "Target: ${s.target}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = s.effect,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (s.restrictions.isNotBlank()) {
                Text(
                    text = "Restrictions: ${s.restrictions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
