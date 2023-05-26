package com.github.rahul_gill.attendance.ui.create

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.TimeChooserBinding
import com.github.rahul_gill.attendance.util.viewBinding

class TimeDayChooserFragment: Fragment(R.layout.time_chooser) {
    val binding by viewBinding(TimeChooserBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}