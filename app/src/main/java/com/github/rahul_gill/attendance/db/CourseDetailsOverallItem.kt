package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseDetailsOverallItem(
    val courseId: Long,
    val courseName: String,
    val requiredAttendance: Double,
    val currentAttendancePercentage: Double,
    val presents: Int = 0,
    val absents: Int = 0,
    val cancels: Int = 0,
    val unsets: Int = 0
) : Parcelable