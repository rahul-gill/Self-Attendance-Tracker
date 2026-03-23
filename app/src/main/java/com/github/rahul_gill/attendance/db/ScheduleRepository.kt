package com.github.rahul_gill.attendance.db

import com.github.rahul_gill.attendance.notification.ClassReminderScheduler
import com.github.rahul_gill.attendance.util.applicationContextGlobal

/**
 * Intermediary between UI callers and [DBOps] for schedule-modifying operations.
 * After every mutation that can affect scheduled alarms, this class triggers
 * [ClassReminderScheduler.scheduleAlarmsForToday] so that alarms stay in sync.
 *
 * Read-only operations (flows, queries) should still be accessed directly via [dbOps].
 */
class ScheduleRepository(
    val dbOps: DBOps = DBOps.instance
) {

    fun createCourse(
        name: String,
        requiredAttendancePercentage: Double,
        schedule: List<ClassDetail>,
    ): Long {
        val courseId = dbOps.createCourse(name, requiredAttendancePercentage, schedule)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
        return courseId
    }

    fun updateCourseDetails(
        id: Long,
        name: String,
        requiredAttendancePercentage: Double,
        schedule: List<ClassDetail>? = null,
    ) {
        dbOps.updateCourseDetails(id, name, requiredAttendancePercentage, schedule)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
    }

    fun addScheduleClassForCourse(
        courseId: Long,
        classDetails: ClassDetail
    ) {
        dbOps.addScheduleClassForCourse(courseId, classDetails)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
    }

    fun deleteScheduleWithId(id: Long) {
        dbOps.deleteScheduleWithId(id)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
    }

    fun createExtraClasses(
        courseId: Long,
        timings: ExtraClassTimings
    ) {
        dbOps.createExtraClasses(courseId, timings)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
    }

    fun deleteCourse(courseId: Long) {
        dbOps.deleteCourse(courseId)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
    }

    fun changeActivateStatusOfScheduleItem(scheduleId: Long, activate: Boolean) {
        dbOps.changeActivateStatusOfScheduleItem(scheduleId, activate)
        ClassReminderScheduler.scheduleAlarmsForToday(applicationContextGlobal)
    }

    companion object {
        val instance: ScheduleRepository by lazy {
            ScheduleRepository()
        }
    }
}
