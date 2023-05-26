package com.github.rahul_gill.attendance.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.rahul_gill.attendance.databinding.DateChooserBinding
import com.github.rahul_gill.attendance.databinding.TimeChooserBinding
import com.github.rahul_gill.attendance.ui.common.DatePickerVH
import com.github.rahul_gill.attendance.ui.common.TimePickerVH
import java.time.LocalDate
import java.time.LocalTime

class AddExtraClassBottomSheetPagerAdapter(
    private val initials: ExtraClassTimings,
    private val onSetDate: (LocalDate) -> Unit,
    private val onSetStartTime: (LocalTime) -> Unit,
    private val onSetEndTime: (LocalTime) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> DatePickerVH(
                DateChooserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                initials.date,
                onSetDate
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