package com.namma.santhe.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SaffronOrange,
    onPrimary = TextOnPrimary,
    primaryContainer = SaffronOrangeLight,
    onPrimaryContainer = SaffronOrangeDark,
    secondary = ForestGreen,
    onSecondary = TextOnPrimary,
    secondaryContainer = ForestGreenLight,
    onSecondaryContainer = ForestGreenDark,
    background = WarmCream,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = WarmCream,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronOrangeLight,
    onPrimary = SaffronOrangeDark,
    primaryContainer = SaffronOrange,
    onPrimaryContainer = TextOnPrimary,
    secondary = ForestGreenLight,
    onSecondary = ForestGreenDark,
    secondaryContainer = ForestGreen,
    onSecondaryContainer = TextOnPrimary,
    background = DarkBackground,
    onBackground = TextOnPrimary,
    surface = DarkSurface,
    onSurface = TextOnPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorRedLight,
    onError = ErrorRed,
    errorContainer = ErrorRed,
    onErrorContainer = TextOnPrimary
)

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun NammaSantheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SantheTypography,
        content = content
    )
}
