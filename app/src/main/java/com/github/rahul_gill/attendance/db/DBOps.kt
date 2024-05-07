package com.github.rahul_gill.attendance.db

import android.content.Context
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.github.rahul_gill.attendance.Database
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.prefs.UnsetClassesBehavior
import com.github.rahul_gill.attendance.util.applicationContextGlobal
import com.github.rahulgill.attendance.Attendance
import com.github.rahulgill.attendance.ExtraClasses
import com.github.rahulgill.attendance.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import java.time.LocalDate

fun getAndroidSqliteDriver(context: Context) =
    AndroidSqliteDriver(Database.Schema, context, "app.db")

fun getSqliteDB(driver: SqlDriver): Database {
    val enumAdapter = EnumColumnAdapter<CourseClassStatus>()
    return Database(
        driver = driver,
        AttendanceAdapter = Attendance.Adapter(
            classStatusAdapter = enumAdapter,
            dateAdapter = LocalDateAdapter
        ),
        ScheduleAdapter = Schedule.Adapter(
            weekdayAdapter = DayOfWeekAdapter,
            startTimeAdapter = LocalTimeAdapter,
            endTimeAdapter = LocalTimeAdapter
        ),
        ExtraClassesAdapter = ExtraClasses.Adapter(
            dateAdapter = LocalDateAdapter,
            startTimeAdapter = LocalTimeAdapter,
            endTimeAdapter = LocalTimeAdapter,
            classStatusAdapter = enumAdapter
        )
    )
}

class DBOps(
    driver: SqlDriver
) {
    val db by lazy { getSqliteDB(driver) }
    private val queries by lazy { db.appQueries }

    fun createCourse(
        name: String,
        requiredAttendancePercentage: Double,
        schedule: List<ClassDetail>,
    ): Long {
        return db.transactionWithResult {
            queries.createCourse(name, requiredAttendancePercentage)
            val courseId = queries.getLastInsertRowID().executeAsOne()
            schedule.forEach { (dayOfWeek, startTime, endTime, _) ->
                queries.createScheduleItemForCourse(
                    courseId = courseId,
                    weekday = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    includedInSchedule = 1
                )
            }
            courseId
        }
    }

    fun updateCourseDetails(
        id: Long,
        name: String,
        requiredAttendancePercentage: Double,
        schedule: List<ClassDetail>? = null,
    ) {
        db.transaction {
            queries.udpateCourse(name, requiredAttendancePercentage, id)
            schedule?.forEach { (dayOfWeek, startTime, endTime, scheduleID) ->
                if (scheduleID != null)
                    queries.updateScheduleItemForCourse(
                        scheduleId = scheduleID,
                        weekday = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        includedInSchedule = 1
                    )
                else {
                    queries.createScheduleItemForCourse(
                        courseId = id,
                        weekday = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        includedInSchedule = 1
                    )
                }
            }
        }
    }

    fun addScheduleClassForCourse(
        courseId: Long,
        classDetails: ClassDetail
    ) {
        queries.createScheduleItemForCourse(
            courseId = courseId,
            weekday = classDetails.dayOfWeek,
            startTime = classDetails.startTime,
            endTime = classDetails.endTime,
            includedInSchedule = 1
        )
    }

    fun getScheduleAndExtraClassesForToday(): Flow<List<Pair<AttendanceRecordHybrid, AttendanceCounts>>> {
        val scheduleClassesFlow: Flow<List<AttendanceRecordHybrid>> = queries.getCourseListForToday(
            mapper = { attendanceId, scheduleId, courseId, courseName, startTime, endTime, classStatus, date ->
                AttendanceRecordHybrid.ScheduledClass(
                    attendanceId = attendanceId,
                    scheduleId = scheduleId,
                    startTime = startTime,
                    endTime = endTime,
                    courseName = courseName,
                    date = date ?: LocalDate.now(),
                    classStatus = CourseClassStatus.fromString(classStatus),
                    courseId = courseId
                )
            }
        ).asFlow().mapToList(Dispatchers.IO)
        val extraClassesFlow: Flow<List<AttendanceRecordHybrid>> =
            queries.getExtraClassesListForToday(mapper = { courseId, courseName, startTime, endTime, classStatus, extraClassId, date ->
                AttendanceRecordHybrid.ExtraClass(
                    extraClassId = extraClassId,
                    startTime = startTime,
                    endTime = endTime,
                    courseName = courseName,
                    date = date,
                    classStatus = classStatus,
                    courseId = courseId
                )
            }).asFlow().mapToList(Dispatchers.IO)
        return scheduleClassesFlow.combine(extraClassesFlow) { list1, list2 ->
            (list1 + list2).sortedByDescending { it.startTime }
        }.map {  attendanceRecords ->
            attendanceRecords.map {
                Pair(it, getCourseAttendancePercentage(it.courseId))
            }
        }
    }


    fun getCoursesDetailsList(): Flow<List<CourseDetailsOverallItem>> {
        return queries.getCoursesDetailsList(
            mapper = { courseId, courseName, requiredAttendance, _, presents, absents, cancels, unsets ->
                val presentsLater = (presents +  if(PreferenceManager.unsetClassesBehavior.value == UnsetClassesBehavior.ConsiderPresent)
                        unsets else 0L).toInt()
                val absentsLater = (absents +  if(PreferenceManager.unsetClassesBehavior.value == UnsetClassesBehavior.ConsiderAbsent)
                    unsets else 0L).toInt()
                CourseDetailsOverallItem(
                    courseId = courseId,
                    courseName = courseName,
                    requiredAttendance = requiredAttendance,
                    currentAttendancePercentage = if (absentsLater + presentsLater == 0) 100.0 else 100.0 * presentsLater / (absentsLater + presentsLater),
                    presents = presentsLater,
                    absents = absentsLater,
                    cancels = cancels.toInt(),
                    unsets = unsets.toInt()
                )
            }
        ).asFlow().mapToList(Dispatchers.IO)
    }

    fun deleteScheduleWithId(id: Long) {
        queries.deleteScheduleItem(id)
    }

    fun deleteScheduleAttendanceRecord(id: Long) {
        queries.deleteScheduleAttendanceRecord(id)
    }

    fun getCoursesDetailsWithId(id: Long): Flow<CourseDetailsOverallItem> {
        return queries.getCoursesDetailsWithId(
            courseId = id,
            mapper = { courseId, courseName, requiredAttendance, _, presents, absents, cancels, unsets ->
                val presentsLater = (presents +  if(PreferenceManager.unsetClassesBehavior.value == UnsetClassesBehavior.ConsiderPresent)
                    unsets else 0L).toInt()
                val absentsLater = (absents +  if(PreferenceManager.unsetClassesBehavior.value == UnsetClassesBehavior.ConsiderAbsent)
                    unsets else 0L).toInt()
                CourseDetailsOverallItem(
                    courseId = courseId,
                    courseName = courseName,
                    requiredAttendance = requiredAttendance,
                    currentAttendancePercentage = if (absentsLater + presentsLater == 0) 100.0 else 100.0 * presentsLater / (absentsLater + presentsLater),
                    presents = presentsLater,
                    absents = absentsLater,
                    cancels = cancels.toInt(),
                    unsets = unsets.toInt()
                )
            }
        ).asFlow().mapToOne(Dispatchers.IO)
    }

    private fun getCourseAttendancePercentage(courseId: Long): AttendanceCounts {
        return queries.getCourseDetailsSingle(
            courseId,
            mapper = { presents, absents, cancels, unsets, requiredPercentage ->
                val presentsLater = (presents +  if(PreferenceManager.unsetClassesBehavior.value == UnsetClassesBehavior.ConsiderPresent)
                    unsets else 0L)
                val absentsLater = (absents +  if(PreferenceManager.unsetClassesBehavior.value == UnsetClassesBehavior.ConsiderAbsent)
                    unsets else 0L)
                AttendanceCounts(
                    if (absentsLater + presentsLater == 0L) 100.0 else 100.0 * presentsLater / (absentsLater + presentsLater),
                    presentsLater, absentsLater, cancels, unsets, requiredPercentage
                )
            }).executeAsOne()
    }

    fun markAttendanceForScheduleClass(
        attendanceId: Long?,
        classStatus: CourseClassStatus,
        scheduleId: Long?,
        date: LocalDate,
        courseId: Long
    ) {
        if (attendanceId != null)
            queries.markAttendance(attendanceId, classStatus, scheduleId, date, courseId)
        else queries.markAttendanceInsert(classStatus, scheduleId, date, courseId)
    }

    fun markAttendanceForExtraClass(
        extraClassId: Long,
        status: CourseClassStatus
    ) = queries.updateExtraClassStatus(extraClassId = extraClassId, status = status)


    fun getScheduleClassesForCourse(courseId: Long) = queries.getScheduleClassesForCourse(
        courseId,
        mapper = { scheduleId, _, weekday, startTime, endTime, includedInSchedule ->
            ClassDetail(weekday, startTime, endTime, scheduleId, includedInSchedule == 1L)
        }).asFlow().mapToList(Dispatchers.IO)


    fun getExtraClassesListForCourse(courseId: Long) = queries.getExtraClasssesForCourse(
        courseId,
        mapper = { extraClassId, _, date, startTime, endTime, classStatus ->
            ExtraClassDetails(extraClassId, courseId, date, startTime, endTime, classStatus)
        }).asFlow().mapToList(Dispatchers.IO)

    fun createExtraClasses(
        courseId: Long,
        timings: ExtraClassTimings
    ) = queries.createExtraClass(
        courseId,
        timings.date,
        timings.startTime,
        timings.endTime,
        CourseClassStatus.Unset
    )

    fun deleteCourse(courseId: Long) = queries.deleteCourse(courseId)

    fun deleteExtraClass(extraClassId: Long) = queries.deleteExtraClass(extraClassId)

    fun getMarkedAttendancesForCourse(courseId: Long): Flow<List<AttendanceRecordHybrid>> {
        return queries.markedAttendancesForCourse(
            courseId = courseId,
            mapper = { entityId,
                       scheduleId,
                       date,
                       startTime,
                       endTime,
                       classStatus,
                       isExtraCLass,
                       courseName,
                       _->
                if (isExtraCLass != 0L) {
                    AttendanceRecordHybrid.ExtraClass(
                        extraClassId = entityId,
                        startTime = startTime,
                        endTime = endTime,
                        courseName = courseName,
                        date = date,
                        classStatus = classStatus,
                        courseId = courseId
                    )
                } else {
                    AttendanceRecordHybrid.ScheduledClass(
                        attendanceId = entityId,
                        scheduleId = scheduleId!!,
                        startTime = startTime,
                        endTime = endTime,
                        courseName = courseName,
                        date = date,
                        classStatus = classStatus,
                        courseId = courseId
                    )
                }
            }
        )
            .asFlow().mapToList(Dispatchers.IO)
    }

    fun changeActivateStatusOfScheduleItem(scheduleId: Long, activate: Boolean) {
        queries.changeActivateStatusOfScheduleItem(scheduleId = scheduleId, activate = if(activate) 1 else 0)
    }

    companion object {
        val instance: DBOps by lazy {
            DBOps(getAndroidSqliteDriver(applicationContextGlobal))
        }
    }
}

data class AttendanceCounts(
    val percent: Double,
    val present: Long,
    val absents: Long,
    val cancels: Long,
    val unsets: Long,
    val requiredPercentage: Double
)