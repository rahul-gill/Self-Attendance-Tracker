package com.github.rahul_gill.attendance.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.util.enableSharedZAxisTransition

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableSharedZAxisTransition()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)

    }
}
