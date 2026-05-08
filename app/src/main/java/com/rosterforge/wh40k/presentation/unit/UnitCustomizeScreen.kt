package com.rosterforge.wh40k.presentation.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.rosterforge.wh40k.domain.model.Unit
import com.rosterforge.wh40k.presentation.common.SectionHeader

@Composable
fun UnitCustomizeScreen(
    onBack: () -> Unit,
    viewModel: UnitCustomizeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            val title = (state as? UnitCustomizeUiState.Success)?.unit?.name ?: ""
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            UnitCustomizeUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is UnitCustomizeUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(s.message)
            }
            is UnitCustomizeUiState.Success -> Content(
                rUnit = s.rosterUnit,
                unit = s.unit,
                padding = padding,
                onModelCount = viewModel::onModelCountChanged,
                onWargearToggle = viewModel::onWargearToggled,
            )
        }
    }
}

@Composable
private fun Content(
    rUnit: RosterUnit,
    unit: Unit,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onModelCount: (Int) -> Unit,
    onWargearToggle: (String, String, Boolean) -> Unit,
) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 96.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (unit.maxModels > unit.minModels) {
            item {
                SectionHeader(title = "Model count")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${unit.minModels}", modifier = Modifier.padding(end = 8.dp))
                    Slider(
                        value = rUnit.modelCount.toFloat(),
                        onValueChange = { onModelCount(it.toInt()) },
                        valueRange = unit.minModels.toFloat()..unit.maxModels.toFloat(),
                        steps = (unit.maxModels - unit.minModels - 1).coerceAtLeast(0),
                        modifier = Modifier.weight(1f),
                    )
                    Text("${unit.maxModels}", modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    text = "Current: ${rUnit.modelCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (unit.wargearOptions.isNotEmpty()) {
            item { SectionHeader(title = "Wargear") }
            unit.wargearOptions.forEach { option ->
                item {
                    Card(elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(option.description, style = MaterialTheme.typography.titleSmall)
                            option.choices.forEach { choice ->
                                val selected = rUnit.selectedWargear.any {
                                    it.optionId == option.id && it.choiceId == choice.id
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = {
                                            onWargearToggle(option.id, choice.id, it)
                                        },
                                    )
                                    Text(choice.name, modifier = Modifier.weight(1f))
                                    if (choice.pointsCost > 0) {
                                        Text("+${choice.pointsCost} pts",
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionHeader(title = "Points")
            Text(
                text = "${rUnit.computedPoints} pts",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
