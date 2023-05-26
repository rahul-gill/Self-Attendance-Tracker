package com.github.rahul_gill.attendance.ui.common

import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.WeekdayChooserBinding
import java.time.DayOfWeek

class WeekDayPickerVH(
    val binding: WeekdayChooserBinding,
    initialWeekday: DayOfWeek,
    onSetWeekDay: (DayOfWeek) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.layoutParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        binding.radioGroup.check(
            when (initialWeekday) {
                DayOfWeek.MONDAY -> R.id.monday
                DayOfWeek.TUESDAY -> R.id.tuesday
                DayOfWeek.WEDNESDAY -> R.id.wednesday
                DayOfWeek.THURSDAY -> R.id.thursday
                DayOfWeek.FRIDAY -> R.id.friday
                DayOfWeek.SATURDAY -> R.id.saturday
                DayOfWeek.SUNDAY -> R.id.sunday
            }
        )
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            onSetWeekDay(
                when (checkedId) {
                    R.id.monday -> DayOfWeek.of(1)
                    R.id.tuesday -> DayOfWeek.of(2)
                    R.id.wednesday -> DayOfWeek.of(3)
                    R.id.thursday -> DayOfWeek.of(4)
                    R.id.friday -> DayOfWeek.of(5)
                    R.id.saturday -> DayOfWeek.of(6)
                    else -> DayOfWeek.of(7)
                }
            )
        }
    }
}