package com.github.rahul_gill.attendance.prefs

import com.github.rahul_gill.attendance.util.LongPreference
import com.github.rahul_gill.attendance.util.StringPreference

const val DEFAULT_TIME_FORMAT = "hh:mm a"
const val DEFAULT_DATE_FORMAT = "d MMM, yyyy"

object PreferenceManager {
    val themePref = LongPreference(key = "app_theme", defaultValue = 0)
    val defaultHomeTabPref =
        LongPreference(key = "default_home_tab", defaultValue = 0)
    val defaultTimeFormatPref =
        StringPreference(key = "default_time_format", defaultValue = DEFAULT_TIME_FORMAT)
    val defaultDateFormatPref =
        StringPreference(key = "default_date_format", defaultValue = DEFAULT_DATE_FORMAT)
}