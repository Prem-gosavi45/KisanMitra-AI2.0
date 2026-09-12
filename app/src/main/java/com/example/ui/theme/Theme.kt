package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Single explicit KisanMitra light palette
val KisanMitraLightColorScheme = lightColorScheme(
    primary = KisanMitraPrimary,
    onPrimary = KisanMitraWhite,
    primaryContainer = KisanMitraPrimaryContainer,
    onPrimaryContainer = KisanMitraTextPrimary,
    secondary = KisanMitraOrange,
    onSecondary = KisanMitraTextPrimary,
    secondaryContainer = KisanMitraSecondaryContainer,
    onSecondaryContainer = KisanMitraTextPrimary,
    tertiary = KisanMitraDarkGreen,
    onTertiary = KisanMitraWhite,
    tertiaryContainer = HarvestOrangeTertiaryContainer,
    onTertiaryContainer = KisanMitraTextPrimary,
    background = KisanMitraBackground,
    onBackground = KisanMitraTextPrimary,
    surface = KisanMitraSurface,
    onSurface = KisanMitraTextPrimary,
    surfaceVariant = KisanMitraSurfaceVariant,
    onSurfaceVariant = KisanMitraTextSecondary,
    outline = KisanMitraDivider,
    outlineVariant = KisanMitraDivider,
    error = KisanMitraError,
    onError = KisanMitraWhite
)

val KisanMitraDarkColorScheme = darkColorScheme(
    primary = KisanMitraPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF144D3A),
    onPrimaryContainer = Color(0xFFE7F4EC),
    secondary = KisanMitraOrange,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF6B431D),
    onSecondaryContainer = Color(0xFFFFE7CF),
    tertiary = Color(0xFF4DBB9C),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF00523C),
    onTertiaryContainer = Color(0xFFD1EAE0),
    background = Color(0xFF121413),
    onBackground = Color(0xFFE2E3E1),
    surface = Color(0xFF1A1C1A),
    onSurface = Color(0xFFE2E3E1),
    surfaceVariant = Color(0xFF404542),
    onSurfaceVariant = Color(0xFFBFC4C0),
    outline = Color(0xFF8A938C),
    outlineVariant = Color(0xFF404542),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) KisanMitraDarkColorScheme else KisanMitraLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
