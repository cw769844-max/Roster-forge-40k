package com.rosterforge.wh40k.presentation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosterforge.wh40k.R
import com.rosterforge.wh40k.presentation.common.SectionHeader

@Composable
fun CreateRosterScreen(
    onContinue: (draftId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateRosterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(title = stringResource(R.string.create_name_label))
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader(title = stringResource(R.string.create_points_label))
            val presets = listOf(500, 1000, 1500, 2000)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                presets.forEachIndexed { index, value ->
                    SegmentedButton(
                        selected = state.pointsLimit == value,
                        onClick = { viewModel.onPointsChange(value) },
                        shape = SegmentedButtonDefaults.itemShape(index, presets.size),
                    ) {
                        Text("$value")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onContinue(viewModel.draftId) },
                enabled = viewModel.canContinue(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.create_continue))
            }
        }
    }
}
