package com.sipos.kebabsk.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = OnBrandOrange,
    primaryContainer = BrandAmberLight,
    onPrimaryContainer = BrownDark,

    secondary = BrandAmber,
    onSecondary = OnBrandAmber,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = BrownDark,

    tertiary = BrownMid,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7CCC8),
    onTertiaryContainer = BrownDark,

    background = CreamBg,
    onBackground = BrownDark,

    surface = CreamSurface,
    onSurface = BrownDark,
    surfaceVariant = Color(0xFFF5E6DC),
    onSurfaceVariant = GreyVariant,

    outline = Color(0xFFBCAAA4),
    outlineVariant = Color(0xFFEDD5C8),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrangeLight,
    onPrimary = Color(0xFF5B1500),
    primaryContainer = DarkOrangeContainer,
    onPrimaryContainer = OnDarkOrangeContainer,

    secondary = BrandAmber,
    onSecondary = Color(0xFF402D00),
    secondaryContainer = Color(0xFF5C4100),
    onSecondaryContainer = Color(0xFFFFDEA1),

    tertiary = Color(0xFFD7CCC8),
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = Color(0xFF5D4037),
    onTertiaryContainer = Color(0xFFEFEBE9),

    background = DarkBg,
    onBackground = Color(0xFFF5DDD5),

    surface = DarkSurface,
    onSurface = Color(0xFFF5DDD5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD7B9AD),

    outline = Color(0xFFA07A6F),
    outlineVariant = Color(0xFF5C3A2F),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun SiposKebabSkTheme(
    darkTheme: Boolean = false, // Force light theme only
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}