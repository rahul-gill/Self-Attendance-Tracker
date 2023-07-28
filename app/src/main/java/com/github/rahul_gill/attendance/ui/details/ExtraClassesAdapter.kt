package com.github.rahul_gill.attendance.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.ExtraClassItemBinding
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.ExtraClassDetails
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.timeFormatter

class ExtraClassesAdapter(
    val onClick: (ExtraClassDetails) -> Unit
) : ListAdapter<ExtraClassDetails, ExtraClassesAdapter.VH>(DiffCallback) {

    inner class VH(
        val binding: ExtraClassItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun getItem(): ExtraClassDetails = getItem(adapterPosition)
        init {
            binding.root.setOnClickListener { onClick(getItem(adapterPosition)) }
        }

        fun bind(item: ExtraClassDetails) {
            binding.apply {
                date.text = item.date.format(dateFormatter)
                timings.text = root.context.getString(R.string.time_range, item.startTime.format(timeFormatter), item.endTime.format(timeFormatter))
                statusCard.setCardBackgroundColor(
                    binding.root.context.getThemeColor(
                        when (item.classStatus) {
                            CourseClassStatus.Present -> R.attr.colorSuccessContainer
                            CourseClassStatus.Absent -> com.google.android.material.R.attr.colorErrorContainer
                            CourseClassStatus.Cancelled -> com.google.android.material.R.attr.colorSurfaceVariant
                            CourseClassStatus.Unset -> com.google.android.material.R.attr.colorSurfaceVariant
                        }
                    )
                )
                statusText.setTextColor(
                    binding.root.context.getThemeColor(
                        when (item.classStatus) {
                            CourseClassStatus.Present -> R.attr.colorOnSuccessContainer
                            CourseClassStatus.Absent -> com.google.android.material.R.attr.colorOnErrorContainer
                            CourseClassStatus.Cancelled -> com.google.android.material.R.attr.colorOnSurfaceVariant
                            CourseClassStatus.Unset -> com.google.android.material.R.attr.colorOnSurfaceVariant
                        }
                    )
                )
                statusText.text = when (item.classStatus) {
                    CourseClassStatus.Present -> "P"
                    CourseClassStatus.Absent -> "A"
                    CourseClassStatus.Cancelled -> "C"
                    CourseClassStatus.Unset -> "~"
                }
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ExtraClassDetails>() {
        override fun areItemsTheSame(oldItem: ExtraClassDetails, newItem: ExtraClassDetails) =
            oldItem.extraClassId == newItem.extraClassId

        override fun areContentsTheSame(oldItem: ExtraClassDetails, newItem: ExtraClassDetails) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ExtraClassItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}