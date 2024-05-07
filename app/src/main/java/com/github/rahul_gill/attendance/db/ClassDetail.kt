package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class ClassDetail constructor(
    val dayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
    val startTime: LocalTime = LocalTime.now().withMinute(0),
    val endTime: LocalTime = startTime.plusHours(1),
    val scheduleId: Long? = null,
    val includedInSchedule: Boolean = true
) : Parcelable