package com.qppd.pesapp.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFCEFAF8),
    onSecondaryContainer = Color(0xFF001F1E),
    tertiary = Color(0xFFEF5350),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD7),
    onTertiaryContainer = Color(0xFF410002),
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF004881),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF70EFEA),
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF004F4D),
    onSecondaryContainer = Color(0xFF70EFEA),
    tertiary = Color(0xFFFFB3AE),
    onTertiary = Color(0xFF680003),
    tertiaryContainer = Color(0xFF930006),
    onTertiaryContainer = Color(0xFFFFDAD7),
    error = Color(0xFFCF6679),
    onError = Color(0xFF140C0D),
    errorContainer = Color(0xFFB1384E),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)


@Composable
fun PESAppTheme(
    schoolTheme: SchoolTheme? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply school theme if available
    val finalColorScheme = schoolTheme?.let { theme ->
        colorScheme.copy(
            primary = Color(android.graphics.Color.parseColor(theme.primaryColor)),
            secondary = Color(android.graphics.Color.parseColor(theme.secondaryColor)),
            tertiary = Color(android.graphics.Color.parseColor(theme.tertiaryColor)),
            error = Color(android.graphics.Color.parseColor(theme.errorColor))
        )
    } ?: colorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = finalColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}