package com.github.rahul_gill.attendance.ui.create

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.TimeChooserBinding
import com.github.rahul_gill.attendance.databinding.WeekdayChooserBinding
import com.github.rahul_gill.attendance.ui.common.TimePickerVH
import com.github.rahul_gill.attendance.ui.common.WeekDayPickerVH
import java.time.DayOfWeek
import java.time.LocalTime

class AddClassBottomSheetPagerAdapter(
    private val initials: ClassDetail,
    val onSetWeekDay: (DayOfWeek) -> Unit,
    val onSetStartTime: (LocalTime) -> Unit,
    val onSetEndTime: (LocalTime) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> WeekDayPickerVH(
                WeekdayChooserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                initials.dayOfWeek,
                onSetWeekDay
            )

            1 -> TimePickerVH(
                TimeChooserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                initials.startTime,
                onSetStartTime
            )

            else -> TimePickerVH(
                TimeChooserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                initials.endTime,
                onSetEndTime
            )
        }
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemCount() = 3

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
}