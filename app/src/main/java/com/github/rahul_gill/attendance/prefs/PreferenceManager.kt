package com.github.rahul_gill.attendance.prefs

const val DEFAULT_TIME_FORMAT = "hh:mm a"
const val DEFAULT_DATE_FORMAT = "d MMM, yyyy"

object PreferenceManager {
    val themePref = object: LongPreference(){
        override val key: String
            get() = "app_theme"
        override val defaultValue: Long
            get() = 0
    }

    val defaultHomeTabPref = object: LongPreference(){
        override val key: String
            get() = "default_home_tab"
        override val defaultValue: Long
            get() = 0
    }

    val defaultTimeFormatPref = object: StringPreference(){
        override val key: String
            get() = "default_time_format"
        override val defaultValue: String
            get() = DEFAULT_TIME_FORMAT
    }


    val defaultDateFormatPref = object: StringPreference(){
        override val key: String
            get() = "default_time_format"
        override val defaultValue: String
            get() = DEFAULT_DATE_FORMAT
    }
}