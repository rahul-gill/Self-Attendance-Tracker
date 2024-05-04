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
) : Parcelable