package com.github.rahul_gill.attendance.ui.common.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rahul_gill.attendance.databinding.AttendanceCalendarPagerItemBinding
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.time.format.DateTimeFormatter

class MonthsPagerAdapter(
    val onDayOfMonthClick: (DaysOfMonthItem) -> Unit,
    val onGoToIndexMonth: (index: Int) -> Unit,
    var itemsForMonth: (LocalDate) -> List<DaysOfMonthItem> = { listOf() }
) : RecyclerView.Adapter<MonthsPagerAdapter.VH>() {
    private val startDate = LocalDate.of(2020, Month.JANUARY, 1)
    private val endDate = LocalDate.now().plusYears(2)

    init {
        setHasStableIds(true)
    }

    class VH(
        val binding: AttendanceCalendarPagerItemBinding
    ) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }


    fun getMonthPosition(date: LocalDate) =
        Period.between(startDate, date).toTotalMonths().toInt()


    override fun onBindViewHolder(holder: VH, position: Int) {
        val month = startDate.plusMonths(position.toLong()).withDayOfMonth(1)
        holder.binding.apply {
            ///header
            previous.isEnabled = position != 0
            next.isEnabled = position != itemCount - 1
            previous.setOnClickListener { onGoToIndexMonth(position - 1) }
            next.setOnClickListener { onGoToIndexMonth(position + 1) }
            monthName.text = month.format(DateTimeFormatter.ofPattern("MMM yyyy"))
            //grid
            dayOfMonthsGrid.layoutManager = GridLayoutManager(root.context, 7)



            dayOfMonthsGrid.adapter = DaysOfMonthGridAdapter(
                itemList = itemsForMonth(month),
                firstDayOfWeek = month.dayOfWeek,
                onDayOfMonthClick = { dayOfMonthItem ->
                    onDayOfMonthClick(dayOfMonthItem)
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        AttendanceCalendarPagerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = Period.between(startDate, endDate).toTotalMonths().toInt()

    override fun getItemId(position: Int) = startDate.plusMonths(position.toLong()).toEpochDay()

}