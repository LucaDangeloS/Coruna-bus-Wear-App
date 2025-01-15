package com.ldangelo.corunabuswear.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val RedTranvias = Color(0xfff34639)
val RedTranvias2 = Color(0xffc0372d)
val RedTranviasSecondary = Color(0xffe30c18)
val RedTranviasSecondary2 = Color(0xffb00912)
val Ash = Color(0xff4c4c4c)
val DarkerAsh = Color(0xff2f2f2f)
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)

private val DarkColorScheme = darkColorScheme(
    primary = RedTranvias2,
    secondary = RedTranviasSecondary2,
    tertiary = Purple200,
    background = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,

    surface = Color(0xFF1C1B1F),
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    /* Other default colors to override
     */
)

private val LightColorScheme = lightColorScheme(
    primary = RedTranvias,
    secondary = RedTranviasSecondary,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    onPrimary = Color.Black,
    onSecondary = Color.White,

    surface = Color(0xFFFFFBFE),
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    /* Other default colors to override
    */
)

@Composable
fun CoruñaBusWearTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}