package com.github.rahul_gill.attendance.ui.common.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.AttendanceCalendarGridItemBinding
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.util.getThemeColor
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import java.time.DayOfWeek
import java.time.LocalTime


data class DaysOfMonthItem(
    val dayOfMonth: Int,
    val classes: List<CalendarClassDetails> = listOf()
)

data class CalendarClassDetails(
    val scheduleIdOrExtraClasId: Long,
    val isExtraClas: Boolean,
    val status: CourseClassStatus,
    val startTime: LocalTime,
    val endTime: LocalTime,
)

class DaysOfMonthGridAdapter(
    private val itemList: List<DaysOfMonthItem>,
    firstDayOfWeek: DayOfWeek,
    val onDayOfMonthClick: (DaysOfMonthItem) -> Unit
) : RecyclerView.Adapter<DaysOfMonthGridAdapter.DaysOfMonthGridVH>() {
    private val firstDayOfMonthIndex = 7 + (firstDayOfWeek.value - 1)

    class DaysOfMonthGridVH(val binding: AttendanceCalendarGridItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    @androidx.annotation.OptIn(ExperimentalBadgeUtils::class)
    private fun addBadge(view: View, number: Int) {
        val badgeDrawable = BadgeDrawable.create(view.context)
        badgeDrawable.number = number
        badgeDrawable.isVisible = true
        BadgeUtils.attachBadgeDrawable(badgeDrawable, view)
    }


    override fun onBindViewHolder(holder: DaysOfMonthGridVH, position: Int) {
        when {
            position < 7 -> {
                holder.binding.root.visibility = View.VISIBLE
                holder.binding.text.text = DayOfWeek.of(position + 1).name.substring(0, 1)
            }

            position < firstDayOfMonthIndex -> {
                holder.binding.root.visibility = View.INVISIBLE
            }

            else -> {
                holder.binding.root.visibility = View.VISIBLE
                holder.binding.text.text = (position - firstDayOfMonthIndex + 1).toString()

                val item = itemList[position - firstDayOfMonthIndex]

                if (item.classes.isNotEmpty()) {
                    addBadge(holder.binding.root, item.classes.size)
                    holder.binding.root.setOnClickListener {
                        onDayOfMonthClick(item)
                    }
                    holder.binding.text.text = DayOfWeek.of(position + 1).name.substring(0, 1)


                    holder.binding.root.setCardBackgroundColor(holder.binding.root.context.getThemeColor(
                        when (item.classes[0].status) {
                            CourseClassStatus.Present -> R.attr.colorSuccessContainer
                            CourseClassStatus.Absent -> com.google.android.material.R.attr.colorErrorContainer
                            CourseClassStatus.Cancelled -> com.google.android.material.R.attr.colorSurfaceVariant
                            CourseClassStatus.Unset -> com.google.android.material.R.attr.colorSurfaceVariant
                        }
                    ))
                    holder.binding.text.setTextColor(holder.binding.root.context.getThemeColor(
                        when (item.classes[0].status) {
                            CourseClassStatus.Present -> R.attr.colorOnSuccessContainer
                            CourseClassStatus.Absent -> com.google.android.material.R.attr.colorOnErrorContainer
                            CourseClassStatus.Cancelled -> com.google.android.material.R.attr.colorOnSurfaceVariant
                            CourseClassStatus.Unset -> com.google.android.material.R.attr.colorOnSurfaceVariant
                        }
                    ))
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DaysOfMonthGridVH(
        AttendanceCalendarGridItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    /*
     * 7 for week day label, some for padding initial week day
     */
    override fun getItemCount() = firstDayOfMonthIndex + itemList.size
}