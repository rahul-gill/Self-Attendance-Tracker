package com.github.rahul_gill.attendance.ui.common

import android.os.Build
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.databinding.TimeChooserBinding
import java.time.LocalTime

class TimePickerVH(
    val binding: TimeChooserBinding,
    initialTime: LocalTime,
    onSetTime: (LocalTime) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.layoutParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.hour = initialTime.hour
            binding.timePicker.minute = initialTime.minute
        }
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            onSetTime(LocalTime.of(hourOfDay, minute))
        }
    }
}