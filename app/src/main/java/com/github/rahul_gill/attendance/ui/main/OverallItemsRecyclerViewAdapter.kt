package com.github.rahul_gill.attendance.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.OverallListItemBinding
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem

class OverallItemsRecyclerViewAdapter(
    val onClick: (View, CourseDetailsOverallItem) -> Unit
) : ListAdapter<CourseDetailsOverallItem, OverallItemsRecyclerViewAdapter.VH>(OverallItemDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            OverallListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: OverallListItemBinding,
        val onClick: (View, CourseDetailsOverallItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(details: CourseDetailsOverallItem) {
            binding.apply {
                root.transitionName = binding.root.resources.getString(
                    R.string.course_detail_card_transition,
                    details.courseId
                )
                root.setOnClickListener {
                    onClick(binding.root, details)
                }
                courseName.text = details.courseName
                attendancePercentage.text = root.context.getString(R.string.double_repr, details.currentAttendancePercentage)
                presentsCount.text = details.presents.toString()
                absentsCount.text = details.absents.toString()
                cancelledCount.text = details.cancels.toString()
            }
        }
    }

    object OverallItemDiffCallBack : DiffUtil.ItemCallback<CourseDetailsOverallItem>() {
        override fun areItemsTheSame(
            oldItem: CourseDetailsOverallItem,
            newItem: CourseDetailsOverallItem
        ) = oldItem.courseId == newItem.courseId

        override fun areContentsTheSame(
            oldItem: CourseDetailsOverallItem,
            newItem: CourseDetailsOverallItem
        ) = oldItem == newItem

    }
}

