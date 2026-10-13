package com.ramitsuri.choresclient.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

private val LightBotanicalColorScheme = lightColorScheme(
    primary = BotanicalGreen,
    onPrimary = OnBotanicalGreen,
    primaryContainer = BotanicalGreenContainer,
    onPrimaryContainer = OnBotanicalGreenContainer,
    secondary = SageSecondary,
    onSecondary = OnSageSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = OnSageSecondaryContainer,
    background = FreshSurface,
    onBackground = OnFreshSurface,
    surface = FreshSurface,
    onSurface = OnFreshSurface,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnFreshSurface,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    error = PastDueCoral,
    errorContainer = PastDueCoralContainer,
    onErrorContainer = OnPastDueCoralContainer
)

private val DarkBotanicalColorScheme = darkColorScheme(
    primary = BotanicalGreenLightDarkTheme,
    onPrimary = OnBotanicalGreenDarkTheme,
    primaryContainer = BotanicalGreenContainerDarkTheme,
    onPrimaryContainer = OnBotanicalGreenContainerDarkTheme,
    secondary = SageSecondaryDarkTheme,
    onSecondary = OnSageSecondaryDarkTheme,
    secondaryContainer = SageSecondaryContainerDarkTheme,
    onSecondaryContainer = OnSageSecondaryContainerDarkTheme,
    background = FreshSurfaceDarkTheme,
    onBackground = OnFreshSurfaceDarkTheme,
    surface = FreshSurfaceDarkTheme,
    onSurface = OnFreshSurfaceDarkTheme,
    surfaceVariant = SurfaceContainerDarkTheme,
    onSurfaceVariant = OnFreshSurfaceDarkTheme,
    surfaceContainerLow = SurfaceContainerLowDarkTheme,
    surfaceContainer = SurfaceContainerDarkTheme,
    surfaceContainerHigh = SurfaceContainerHighDarkTheme,
    surfaceContainerHighest = SurfaceContainerHighestDarkTheme,
    error = PastDueCoral,
    errorContainer = PastDueCoralContainer,
    onErrorContainer = OnPastDueCoralContainer
)

@Composable
fun ChoresClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    dimens: Dimens = Dimens(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkBotanicalColorScheme
        else -> LightBotanicalColorScheme
    }

    CompositionLocalProvider(LocalDimensions provides dimens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.dimens: Dimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current
