package com.github.rahul_gill.attendance.prefs

import androidx.compose.ui.graphics.Color
import com.github.rahul_gill.attendance.ui.compose.comps.DarkThemeType
import com.github.rahul_gill.attendance.ui.compose.comps.ThemeConfig
import com.github.rahul_gill.attendance.util.BooleanPreference
import com.github.rahul_gill.attendance.util.IntPreference
import com.github.rahul_gill.attendance.util.LongPreference
import com.github.rahul_gill.attendance.util.StringPreference
import com.github.rahul_gill.attendance.util.customPreference
import com.github.rahul_gill.attendance.util.enumPreference

const val DEFAULT_TIME_FORMAT = "hh:mm a"
const val DEFAULT_DATE_FORMAT = "d MMM, yyyy"

object PreferenceManager {
    val themePref = LongPreference(key = "app_theme", defaultValue = 0)
    val themeConfig = enumPreference(
        key = "theme_config",
        defaultValue = ThemeConfig.FollowSystem
    )
    val darkThemeType = enumPreference(
        key = "dark_theme_type",
        defaultValue = DarkThemeType.Dark
    )
    val followSystemColors = BooleanPreference(key = "follow_system_colors", defaultValue = false)
    val colorSchemeSeed = customPreference(
        backingPref = LongPreference("color_scheme_type", 0),
        defaultValue = Color(0xFF9867C5),
        serialize = { color -> color.value.toLong() },
        deserialize = { if(it == 0L)  Color(0xFF9867C5) else  Color(it.toULong()) }
    )

    val defaultHomeTabPref =
        IntPreference(key = "default_home_tab", defaultValue = 0)
    val defaultTimeFormatPref =
        StringPreference(key = "default_time_format", defaultValue = DEFAULT_TIME_FORMAT)
    val defaultDateFormatPref =
        StringPreference(key = "default_date_format", defaultValue = DEFAULT_DATE_FORMAT)
}