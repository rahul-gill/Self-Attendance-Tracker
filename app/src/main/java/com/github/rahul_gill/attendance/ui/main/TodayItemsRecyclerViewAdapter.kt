package com.github.rahul_gill.attendance.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.TodayItemBinding
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.timeFormatter


class TodayItemsRecyclerViewAdapter(
    private val onClick: (TodayCourseItem) -> Unit
) : ListAdapter<TodayCourseItem, TodayItemsRecyclerViewAdapter.VH>(TodayItemDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(TodayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), onClick)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: TodayItemBinding,
        private val onClick: (TodayCourseItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(details: TodayCourseItem) {
            binding.apply {
                root.setOnClickListener {
                    onClick(details)
                }
                extraClassIndication.isVisible = details.isExtraClass

                courseName.text = details.courseName
                startTime.text = details.startTime.format(timeFormatter)
                endTime.text = details.endTime.format(timeFormatter)
                attendanceStatusContainer.setCardBackgroundColor(
                    binding.root.context.getThemeColor(
                        when (details.classStatus) {
                            CourseClassStatus.Present -> R.attr.colorSuccessContainer
                            CourseClassStatus.Absent -> com.google.android.material.R.attr.colorErrorContainer
                            CourseClassStatus.Cancelled -> com.google.android.material.R.attr.colorSurfaceVariant
                            CourseClassStatus.Unset -> com.google.android.material.R.attr.colorSurfaceVariant
                        }
                    )
                )
                attendanceStatusText.text = when (details.classStatus) {
                    CourseClassStatus.Present -> "P"
                    CourseClassStatus.Absent -> "A"
                    CourseClassStatus.Cancelled -> "C"
                    CourseClassStatus.Unset -> "~"
                }
                attendanceStatusText.setTextColor(
                    binding.root.context.getThemeColor(
                        when (details.classStatus) {
                            CourseClassStatus.Present -> R.attr.colorOnSuccessContainer
                            CourseClassStatus.Absent -> com.google.android.material.R.attr.colorOnErrorContainer
                            CourseClassStatus.Cancelled -> com.google.android.material.R.attr.colorOnSurfaceVariant
                            CourseClassStatus.Unset -> com.google.android.material.R.attr.colorOnSurfaceVariant
                        }
                    )
                )
            }
        }
    }

    object TodayItemDiffCallBack : DiffUtil.ItemCallback<TodayCourseItem>() {
        override fun areItemsTheSame(
            oldItem: TodayCourseItem,
            newItem: TodayCourseItem
        ) = oldItem.scheduleIdOrExtraClassId == newItem.scheduleIdOrExtraClassId

        override fun areContentsTheSame(
            oldItem: TodayCourseItem,
            newItem: TodayCourseItem
        ) = oldItem == newItem

    }
}