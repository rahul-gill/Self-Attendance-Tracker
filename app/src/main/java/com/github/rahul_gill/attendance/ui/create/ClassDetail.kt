package com.github.rahul_gill.attendance.ui.create

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class ClassDetail(
    val dayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
    val startTime: LocalTime = LocalTime.now(),
    val endTime: LocalTime = LocalTime.now(),
    val scheduleId: Long? = null,
) : Parcelable