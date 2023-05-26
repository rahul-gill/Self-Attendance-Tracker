package com.github.rahul_gill.attendance.ui.create

import android.os.Bundle
import android.view.View
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.WeekdayChooserBinding
import com.github.rahul_gill.attendance.util.BaseFragment
import com.github.rahul_gill.attendance.util.viewBinding
import java.time.DayOfWeek

class WeekDayChooserFragment : BaseFragment(R.layout.weekday_chooser) {
    val binding by viewBinding(WeekdayChooserBinding::bind)
    private val weekDay = savedStateOf("weekday", DayOfWeek.MONDAY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.radioGroup.check(R.id.monday)
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            weekDay.value = when (checkedId) {
                R.id.monday -> DayOfWeek.of(1)
                R.id.tuesday -> DayOfWeek.of(2)
                R.id.wednesday -> DayOfWeek.of(3)
                R.id.thursday -> DayOfWeek.of(4)
                R.id.friday -> DayOfWeek.of(5)
                R.id.saturday -> DayOfWeek.of(6)
                else -> DayOfWeek.of(7)
            }
        }
    }
}