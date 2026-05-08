package com.rosterforge.wh40k.presentation.unit

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.presentation.common.SectionHeader

@Composable
fun UnitBrowserScreen(
    onUnitAdded: (rosterId: String, rosterUnitId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: UnitBrowserViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UnitBrowserEvent.UnitAdded -> onUnitAdded(event.rosterId, event.rosterUnitId)
                is UnitBrowserEvent.Failed -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Unit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when (val s = state) {
            UnitBrowserUiState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            is UnitBrowserUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(s.message)
            }
            is UnitBrowserUiState.Success -> UnitList(
                grouped = s.unitsByRole,
                padding = padding,
                onClick = viewModel::onUnitChosen,
            )
        }
    }
}

@Composable
private fun UnitList(
    grouped: Map<com.rosterforge.wh40k.domain.model.BattlefieldRole, List<Unit>>,
    padding: PaddingValues,
    onClick: (Unit) -> Unit,
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
        grouped.forEach { (role, units) ->
            item { SectionHeader(title = role.name) }
            items(units, key = { it.id }) { unit -> UnitRow(unit, onClick) }
        }
    }
}

@Composable
private fun UnitRow(unit: Unit, onClick: (Unit) -> Unit) {
    val basePoints = unit.pointsCosts.minByOrNull { it.modelCount }?.points ?: 0
    Card(
        onClick = { onClick(unit) },
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(unit.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = unit.keywords.take(3).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "$basePoints pts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
