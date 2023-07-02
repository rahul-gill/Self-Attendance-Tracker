package com.github.rahul_gill.attendance.ui.details.sticky

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.databinding.AttendanceRecordItemBinding
import com.github.rahul_gill.attendance.databinding.AttendanceRecordItemHeaderBinding
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.getThemeColor
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahulgill.attendance.MarkedAttendancesForCourse
import java.time.LocalDate

sealed class AttendanceRecordListItem{
    class Header(val date: LocalDate): AttendanceRecordListItem()
    class Item(val item: MarkedAttendancesForCourse): AttendanceRecordListItem()
}

class AttendanceRecordRecyclerViewAdapter(
    private val onItemClick: (MarkedAttendancesForCourse) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listItems: List<AttendanceRecordListItem> = listOf()

    class VH(
        val onItemClick: (MarkedAttendancesForCourse) -> Unit,
        val binding: AttendanceRecordItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(details: MarkedAttendancesForCourse) = binding.apply{
            root.setOnClickListener {
                onItemClick(details)
            }
            classType.text = if(details.isExtraCLass == 0L) "Scheduled Class" else "Extra Class"

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

    class HeaderVH(val binding: AttendanceRecordItemHeaderBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
         = if(viewType == 0){
            VH(onItemClick, AttendanceRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else{
            HeaderVH(AttendanceRecordItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is VH){
            val item = (listItems[position] as AttendanceRecordListItem.Item).item
            holder.bind(item)
        } else if(holder is HeaderVH){
            holder.binding.dateText.text = dateFormatter.format((listItems[position] as AttendanceRecordListItem.Header).date)
        }
    }

    override fun getItemCount() = listItems.size

    override fun getItemViewType(position: Int): Int {
        return if(listItems[position] is AttendanceRecordListItem.Header) 1
        else 0
    }

    fun submitList(
        items: List<MarkedAttendancesForCourse>
    ){
        val list = mutableListOf<AttendanceRecordListItem>()
        items
            .groupBy { it.date }
            .toList()
            .sortedBy { it.first }
            .forEach { (date, itemsOnDate) ->
                list.add(AttendanceRecordListItem.Header(date))
                itemsOnDate.forEach { list.add(AttendanceRecordListItem.Item(it)) }
            }
        listItems = list
        notifyDataSetChanged()
    }

}