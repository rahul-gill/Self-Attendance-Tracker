package com.github.rahul_gill.attendance.ui.common

import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.databinding.DateChooserBinding
import java.time.LocalDate

class DatePickerVH (
    val binding: DateChooserBinding,
    initialDate: LocalDate,
    onSetDate: (LocalDate) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.datePicker.init(initialDate.year, initialDate.monthValue, initialDate.dayOfMonth){
            _, year, month, day ->
            onSetDate(LocalDate.of(year, month, day))
        }
    }
}