package com.github.rahul_gill.attendance.prefs

import android.util.Log
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


private val settings: Settings by lazy { Settings() }
private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

interface Preference<T> {
    fun setValue(value: T)
    val value: T
    val observableValue: StateFlow<T>
    val key: String
    val defaultValue: T
}

@OptIn(ExperimentalSettingsApi::class)
abstract class LongPreference(
) : Preference<Long>{
    override fun setValue(value: Long) {
        settings.putLong(key, value)
    }

    override val value: Long
        get() = settings.getLong(key, defaultValue)

    override val observableValue: StateFlow<Long>
        get() = observableSettings.getLongFlow(key, defaultValue)
            .stateIn(GlobalScope, SharingStarted.Eagerly, value)
}


@OptIn(ExperimentalSettingsApi::class)
abstract class StringPreference(
) : Preference<String>{
    override fun setValue(value: String) {
        settings.putString(key, value)
    }

    override val value: String
        get() =settings.getString(key, defaultValue)

    override val observableValue: StateFlow<String>
        get() = observableSettings.getStringFlow(key, defaultValue)
            .stateIn(GlobalScope, SharingStarted.Eagerly, value)
}
