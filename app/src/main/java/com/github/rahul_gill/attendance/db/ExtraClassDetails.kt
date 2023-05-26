package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import com.github.rahulgill.attendance.ExtraClasses
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class ExtraClassDetails(
    public val extraClassId: Long,
    public val courseId: Long,
    public val date: LocalDate,
    public val startTime: LocalTime,
    public val endTime: LocalTime,
    public val classStatus: CourseClassStatus,
) : Parcelable