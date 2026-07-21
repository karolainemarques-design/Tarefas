package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SageGreenPrimaryDark,
    secondary = SageGreenSecondaryDark,
    tertiary = WarmAmberDark,
    background = DarkBackground,
    surface = DarkSurfaceCard,
    onPrimary = Color(0xFF00391A),
    onSecondary = Color(0xFF0E3820),
    onTertiary = Color(0xFF432A00),
    onBackground = Color(0xFFE1E3DF),
    onSurface = Color(0xFFE1E3DF)
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    secondary = SageGreenSecondary,
    tertiary = WarmAmberTertiary,
    background = SoftBackgroundLight,
    surface = SurfaceCardLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF191C19),
    onSurface = Color(0xFF191C19)
)

@Composable
fun TarefasDomesticasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom warm theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

