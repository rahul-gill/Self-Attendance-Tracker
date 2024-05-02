package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import com.github.rahulgill.attendance.MarkedAttendancesForCourse
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class TodayCourseItem(
    val scheduleIdOrExtraClassId: Long,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val classStatus: CourseClassStatus,
    val isExtraClass: Boolean = false,
    val date: LocalDate? = null
) : Parcelable {


    companion object {
        fun fromMarkedAttendancesForCourse(item: MarkedAttendancesForCourse, courseName: String): TodayCourseItem {
            return TodayCourseItem(
                scheduleIdOrExtraClassId = item.entityId,
                isExtraClass = item.isExtraCLass != 0L,
                date = item.date,
                startTime = item.startTime,
                endTime = item.endTime,
                courseName = courseName,
                classStatus = item.classStatus
            )
        }
    }
}
