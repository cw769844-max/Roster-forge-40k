package com.rosterforge.wh40k.presentation.validation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosterforge.wh40k.domain.model.Severity
import com.rosterforge.wh40k.domain.model.ValidationIssue
import com.rosterforge.wh40k.presentation.common.PointsBar
import com.rosterforge.wh40k.presentation.common.SectionHeader
import com.rosterforge.wh40k.presentation.theme.ErrorRed
import com.rosterforge.wh40k.presentation.theme.SuccessGreen
import com.rosterforge.wh40k.presentation.theme.WarningAmber

@Composable
fun ValidationScreen(
    onBack: () -> Unit,
    onViewRoster: (String) -> Unit,
    viewModel: ValidationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            ValidationUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is ValidationUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(s.message)
            }
            is ValidationUiState.Success -> Content(
                state = s,
                padding = padding,
                onViewRoster = { onViewRoster(viewModel.rosterId) },
            )
        }
    }
}

@Composable
private fun Content(
    state: ValidationUiState.Success,
    padding: PaddingValues,
    onViewRoster: () -> Unit,
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
            val color = if (state.result.isLegal) SuccessGreen else ErrorRed
            val text = if (state.result.isLegal) "Roster is legal"
            else "Roster has ${state.result.errors.size} error(s)"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(16.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                )
            }
            PointsBar(state.result.pointsUsed, state.result.pointsLimit)
        }
        if (state.result.errors.isNotEmpty()) {
            item { SectionHeader("Errors") }
            items(state.result.errors) { issue -> IssueCard(issue) }
        }
        if (state.result.warnings.isNotEmpty()) {
            item { SectionHeader("Warnings") }
            items(state.result.warnings) { issue -> IssueCard(issue) }
        }
        item {
            OutlinedButton(onClick = onViewRoster) {
                Text("View Roster")
            }
        }
    }
}

@Composable
private fun IssueCard(issue: ValidationIssue) {
    val color = when (issue.severity) {
        Severity.ERROR -> ErrorRed
        Severity.WARNING -> WarningAmber
        Severity.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = issue.code.name,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
            Text(
                text = issue.message,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
