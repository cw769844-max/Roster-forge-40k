package com.rosterforge.wh40k.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = CrimsonBright,
    onPrimary = OnDarkPrimary,
    primaryContainer = CrimsonDeep,
    onPrimaryContainer = OnDarkPrimary,
    secondary = GoldAccent,
    onSecondary = CharcoalBg,
    secondaryContainer = GoldDim,
    onSecondaryContainer = OnDarkPrimary,
    tertiary = GoldAccent,
    background = CharcoalBg,
    onBackground = OnDarkPrimary,
    surface = CharcoalSurface,
    onSurface = OnDarkPrimary,
    surfaceVariant = CharcoalRaised,
    onSurfaceVariant = OnDarkSecondary,
    error = ErrorRed,
    onError = OnDarkPrimary,
)

private val LightColors = lightColorScheme(
    primary = CrimsonDeep,
    onPrimary = OffWhiteSurface,
    primaryContainer = CrimsonBright,
    onPrimaryContainer = OffWhiteSurface,
    secondary = GoldDim,
    onSecondary = OffWhiteSurface,
    secondaryContainer = GoldAccent,
    onSecondaryContainer = OnLightPrimary,
    tertiary = GoldDim,
    background = OffWhiteBg,
    onBackground = OnLightPrimary,
    surface = OffWhiteSurface,
    onSurface = OnLightPrimary,
    surfaceVariant = OffWhiteBg,
    onSurfaceVariant = OnLightSecondary,
    error = ErrorRed,
    onError = OffWhiteSurface,
)

@Composable
fun RosterForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = RosterForgeTypography,
        content = content,
    )
}
