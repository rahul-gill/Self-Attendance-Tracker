package com.github.rahul_gill.attendance.ui.create

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime

@Parcelize
data class ClassDetail(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val scheduleId: Long? = null,
) : Parcelable