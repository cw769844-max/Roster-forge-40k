package com.rosterforge.wh40k.presentation.view

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
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.presentation.common.SectionHeader

@Composable
fun ViewRosterScreen(
    onBack: () -> Unit,
    onOpenStratagems: (rosterId: String) -> Unit,
    viewModel: ViewRosterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            val title = (state as? ViewRosterUiState.Success)?.roster?.name ?: "Roster"
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenStratagems(viewModel.rosterId) }) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = "Stratagems")
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            ViewRosterUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is ViewRosterUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(s.message)
            }
            is ViewRosterUiState.Success -> Content(s, padding)
        }
    }
}

@Composable
private fun Content(s: ViewRosterUiState.Success, padding: PaddingValues) {
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
            Text(
                text = "${s.faction.name} • ${s.detachment.name}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${s.roster.totalPoints} / ${s.roster.pointsLimit} pts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { SectionHeader("Detachment Rule") }
        item {
            Card(elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.detachment.rule.name, style = MaterialTheme.typography.titleSmall)
                    Text(s.detachment.rule.effect, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        s.unitsByRole.forEach { (role, units) ->
            item { SectionHeader("${role.name} (${units.size})") }
            // Render only units that are NOT attached as leaders to others; the
            // attached leaders are nested inside their bodyguard's card below.
            val attachedLeaderIds = s.attachedLeaderMap.values.map { it.id }.toSet()
            items(units.filter { it.id !in attachedLeaderIds }, key = { it.id }) { unit ->
                UnitCard(unit, attachedLeader = s.attachedLeaderMap[unit.id])
            }
        }
    }
}

@Composable
private fun UnitCard(unit: RosterUnit, attachedLeader: RosterUnit?) {
    Card(elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = unit.unitName + if (unit.modelCount > 1) " (${unit.modelCount})" else "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text("${unit.computedPoints} pts", style = MaterialTheme.typography.titleSmall)
            }
            if (attachedLeader != null) {
                Text(
                    text = "Leader: ${attachedLeader.unitName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            if (unit.selectedWargear.isNotEmpty()) {
                Text(
                    text = unit.selectedWargear.size.toString() +
                        " wargear selection(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
