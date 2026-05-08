package com.rosterforge.wh40k.presentation.enhancement

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

@Composable
fun EnhancementScreen(
    onBack: () -> Unit,
    viewModel: EnhancementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enhancement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            EnhancementUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is EnhancementUiState.Success -> Content(s, padding, viewModel::onSelect)
        }
    }
}

@Composable
private fun Content(
    s: EnhancementUiState.Success,
    padding: PaddingValues,
    onSelect: (com.rosterforge.wh40k.domain.model.Enhancement?) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("For: ${s.unitName}", style = MaterialTheme.typography.titleMedium)
        }
        items(s.items, key = { it.enhancement.id }) { item ->
            val available = item.eligible && !item.takenElsewhere
            Card(
                onClick = { if (available) onSelect(item.enhancement) },
                elevation = CardDefaults.cardElevation(if (available) 1.dp else 0.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.foundation.layout.Row {
                        Text(item.enhancement.name, style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f))
                        Text("${item.enhancement.points} pts",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                    Text(
                        text = item.enhancement.effect,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val status = when {
                        s.currentSelection == item.enhancement.id -> "Currently selected"
                        item.takenElsewhere -> "Already taken"
                        !item.eligible -> "Unit not eligible"
                        else -> null
                    }
                    if (status != null) {
                        Text(status, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = { onSelect(null) }) {
                Text("Clear enhancement")
            }
        }
    }
}
