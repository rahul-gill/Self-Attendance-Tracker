package com.github.rahul_gill.attendance.ui.compose.comps

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.dynamicColorScheme

enum class DarkThemeType {
    Dark, Black
}

enum class ThemeConfig {
    FollowSystem, Light, Dark
}

sealed interface ColorSchemeType {
    class WithSeed(val seed: Color = Color.Blue) : ColorSchemeType
    data object Dynamic : ColorSchemeType
}

private val DefaultSeed = Color.Green

@Composable
fun AttendanceAppTheme(
    colorSchemeType: ColorSchemeType = ColorSchemeType.Dynamic,
    themeConfig: ThemeConfig = ThemeConfig.FollowSystem,
    darkThemeType: DarkThemeType = DarkThemeType.Dark,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme =
        themeConfig == ThemeConfig.Dark || (themeConfig == ThemeConfig.FollowSystem && isSystemInDarkTheme())
    val colorScheme = remember(colorSchemeType, isDarkTheme, darkThemeType) {
        val scheme = when (colorSchemeType) {
            ColorSchemeType.Dynamic -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isDarkTheme)
                        dynamicDarkColorScheme(context)
                    else
                        dynamicLightColorScheme(context)
                } else {
                    dynamicColorScheme(DefaultSeed, isDarkTheme)
                }
            }

            is ColorSchemeType.WithSeed -> {
                dynamicColorScheme(colorSchemeType.seed, isDarkTheme)
            }
        }
        scheme.copy(
            background = if (isDarkTheme && darkThemeType == DarkThemeType.Black) Color.Black else scheme.background,
            surface = if (isDarkTheme && darkThemeType == DarkThemeType.Black) Color.Black else scheme.surface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )

}