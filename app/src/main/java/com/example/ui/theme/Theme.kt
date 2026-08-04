package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ImmersiveColorScheme = darkColorScheme(
    primary = immersive_primary,
    onPrimary = immersive_onPrimary,
    primaryContainer = immersive_primaryContainer,
    onPrimaryContainer = immersive_onPrimaryContainer,
    secondary = immersive_secondary,
    onSecondary = immersive_onSecondary,
    secondaryContainer = immersive_secondaryContainer,
    onSecondaryContainer = immersive_onSecondaryContainer,
    tertiary = immersive_tertiary,
    onTertiary = immersive_onTertiary,
    tertiaryContainer = immersive_tertiaryContainer,
    onTertiaryContainer = immersive_onTertiaryContainer,
    error = immersive_error,
    errorContainer = immersive_errorContainer,
    onError = immersive_onError,
    onErrorContainer = immersive_onErrorContainer,
    background = immersive_background,
    onBackground = immersive_onBackground,
    surface = immersive_surface,
    onSurface = immersive_onSurface,
    surfaceVariant = immersive_surfaceVariant,
    onSurfaceVariant = immersive_onSurfaceVariant,
    outline = immersive_outline,
    inverseOnSurface = immersive_inverseOnSurface,
    inverseSurface = immersive_inverseSurface,
    inversePrimary = immersive_inversePrimary,
    outlineVariant = immersive_outlineVariant,
    surfaceContainerHighest = immersive_surfaceContainerHighest,
    surfaceContainerHigh = immersive_surfaceContainerHigh,
    surfaceContainer = immersive_surfaceContainer,
    surfaceContainerLow = immersive_surfaceContainerLow,
    surfaceContainerLowest = immersive_surfaceContainerLowest,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to force Immersive UI theme
    content: @Composable () -> Unit,
) {
    val colorScheme = ImmersiveColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
