package com.github.rahul_gill.attendance.prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreference
import com.github.rahul_gill.attendance.R


class MaterialYouSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : SwitchPreference(context, attrs, defStyleAttr) {
    init {
        layoutResource = R.layout.material_preference_switch
    }
}