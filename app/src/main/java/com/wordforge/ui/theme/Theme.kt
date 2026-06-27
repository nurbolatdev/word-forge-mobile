package com.wordforge.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Joy,
    secondary = InkSoft,
    tertiary = Border,
    background = Ink,
    surface = InkDark,
    onPrimary = Ink,
    onSecondary = Bg,
    onTertiary = Bg,
    onBackground = Bg,
    onSurface = Bg,
    error = ErrorRed,
    outline = Border
)

private val LightColorScheme = lightColorScheme(
    primary = Joy,
    secondary = InkSoft,
    tertiary = Border,
    background = Bg,
    surface = SurfaceColor,
    onPrimary = Ink,
    onSecondary = Bg,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    error = ErrorRed,
    outline = Border
)

@Composable
fun WordForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
