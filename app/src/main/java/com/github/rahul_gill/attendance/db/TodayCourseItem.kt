package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

@Parcelize
data class TodayCourseItem(
    val scheduleIdOrExtraClassId: Long,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val classStatus: CourseClassStatus,
    val isExtraClass: Boolean = false
) : Parcelable
