package com.github.rahul_gill.attendance.db

import android.os.Parcelable
import com.github.rahulgill.attendance.MarkedAttendancesForCourse
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

sealed interface AttendanceRecordHybrid : Parcelable {
    val courseName: String
    val startTime: LocalTime
    val endTime: LocalTime
    val classStatus: CourseClassStatus
    val date: LocalDate


    /**
     * Attendance id null when we're just showing a record in today item but the item is not yet created
     * scheduledId might be null if schedule is deleted but the attendance record is created
     */
    @Parcelize
    class ScheduledClass(
        val attendanceId: Long?,
        val scheduleId: Long?,
        override val courseName: String,
        override val startTime: LocalTime,
        override val endTime: LocalTime,
        override val classStatus: CourseClassStatus,
        override val date: LocalDate,
    ) : AttendanceRecordHybrid

    @Parcelize
    class ExtraClass(
        val extraClassId: Long,
        override val courseName: String,
        override val startTime: LocalTime,
        override val endTime: LocalTime,
        override val classStatus: CourseClassStatus,
        override val date: LocalDate,
    ) : AttendanceRecordHybrid
}

@Parcelize
data class TodayCourseItem(
    val attendanceIdOrExtraClassId: Long,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val classStatus: CourseClassStatus,
    val isExtraClass: Boolean = false,
    val date: LocalDate? = null
) : Parcelable {


    companion object {
        fun fromMarkedAttendancesForCourse(
            item: MarkedAttendancesForCourse, courseName: String
        ): TodayCourseItem {
            return TodayCourseItem(
                attendanceIdOrExtraClassId = item.entityId,
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
