package com.rosterforge.wh40k.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rosterforge.wh40k.presentation.theme.ErrorRed
import com.rosterforge.wh40k.presentation.theme.SuccessGreen
import com.rosterforge.wh40k.presentation.theme.WarningAmber

@Composable
fun PointsBar(
    used: Int,
    limit: Int,
    modifier: Modifier = Modifier,
) {
    val ratio = if (limit > 0) used.toFloat() / limit.toFloat() else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = ratio.coerceAtMost(1.25f),
        label = "pointsRatio",
    )
    val targetColor = when {
        used > limit -> ErrorRed
        ratio >= 0.95f -> WarningAmber
        else -> SuccessGreen
    }
    val barColor by animateColorAsState(targetColor, label = "barColor")

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
                    .fillMaxWidth(animatedRatio.coerceAtMost(1f)),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$used / $limit pts",
                style = MaterialTheme.typography.labelLarge,
                color = barColor,
            )
            Spacer(Modifier.weight(1f))
            val remaining = limit - used
            Text(
                text = if (remaining >= 0) "$remaining left" else "${-remaining} over",
                style = MaterialTheme.typography.labelSmall,
                color = if (remaining < 0) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
    }
}
