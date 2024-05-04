package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime


@Parcelize
data class ExtraClassTimings(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
) : Parcelable{
    companion object{
        fun defaultTimeAdjusted(): ExtraClassTimings {
            val start = LocalTime.now().withMinute(0)
            return ExtraClassTimings(
                date = LocalDate.now(),
                startTime = start,
                endTime = start.plusHours(1)
            )
        }
    }
}