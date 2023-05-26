package com.github.rahul_gill.attendance.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.databinding.WeeklyScheduleItemBinding
import com.github.rahul_gill.attendance.ui.create.ClassDetail
import com.github.rahul_gill.attendance.util.timeFormatter

class ScheduleClassesAdapter : ListAdapter<ClassDetail, ScheduleClassesAdapter.VH>(DiffCallback) {

    class VH(val binding: WeeklyScheduleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ClassDetail) {
            binding.apply {
                weekday.text = item.dayOfWeek.name
                timings.text = "${item.startTime.format(timeFormatter)} to ${item.endTime.format(timeFormatter)}"
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ClassDetail>() {
        override fun areItemsTheSame(oldItem: ClassDetail, newItem: ClassDetail) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ClassDetail, newItem: ClassDetail) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(WeeklyScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}