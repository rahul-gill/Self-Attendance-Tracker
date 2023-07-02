package com.github.rahul_gill.attendance.db

import android.content.Context
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.github.rahul_gill.attendance.Database
import com.github.rahul_gill.attendance.ui.create.ClassDetail
import com.github.rahul_gill.attendance.ui.details.ExtraClassTimings
import com.github.rahulgill.attendance.Attendance
import com.github.rahulgill.attendance.ExtraClasses
import com.github.rahulgill.attendance.MarkedAttendancesForCourse
import com.github.rahulgill.attendance.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

class DBOps private constructor(
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

    /**
     * null means not to update
     */
    fun updateCourse(
        courseId: Long,
        name: String? = null,
        requiredAttendancePercentage: Double? = null,
        schedule: List<ClassDetail>? = null,
    ) {
        db.transactionWithResult {
            //TODO
        }
    }

    fun getCourseListForToday() = queries.getCourseListForToday(
        mapper = { scheduleId, courseName, startTime, endTime, classStatus ->
            TodayCourseItem(
                scheduleId,
                courseName,
                startTime,
                endTime,
                CourseClassStatus.fromString(classStatus)
            )
        }
    ).asFlow().mapToList(Dispatchers.IO)

    fun getScheduleAndExtraClassesForToday(): Flow<List<TodayCourseItem>> {
        val scheduleClassesFlow = queries.getCourseListForToday(
            mapper = { scheduleId, courseName, startTime, endTime, classStatus ->
                TodayCourseItem(
                    scheduleId,
                    courseName,
                    startTime,
                    endTime,
                    CourseClassStatus.fromString(classStatus)
                )
            }
        ).asFlow().mapToList(Dispatchers.IO)
        val extraClassesFlow =
            queries.getExtraClassesListForToday(mapper = { courseName, startTime, endTime, classStatus, extraClassId ->
                TodayCourseItem(
                    extraClassId,
                    courseName,
                    startTime,
                    endTime,
                    classStatus,
                    isExtraClass = true
                )
            }).asFlow().mapToList(Dispatchers.IO)
        return scheduleClassesFlow.combine(extraClassesFlow) { list1, list2 ->
            (list1 + list2).sortedByDescending { it.startTime }
        }
    }


    fun getCoursesDetailsList(): Flow<List<CourseDetailsOverallItem>> {
        return queries.getCoursesDetailsList(
            mapper = { courseId, courseName, requiredAttendance, _, presents, absents, cancels ->
                CourseDetailsOverallItem(
                    courseId = courseId,
                    courseName = courseName,
                    requiredAttendance = requiredAttendance,
                    currentAttendancePercentage = if (absents + presents == 0L) 100.0 else 100.0 * presents / (presents + absents),
                    presents = presents.toInt(),
                    absents = absents.toInt(),
                    cancels = cancels.toInt()
                )
            }
        ).asFlow().mapToList(Dispatchers.IO)
    }

    fun getCourseAttendancePercentage(courseId: Long): Flow<AttendanceCounts> {
        return queries.getCourseDetailsSingle(courseId, mapper = { presents, absents, cancels, requiredPercentage ->
            AttendanceCounts(
                if (absents + presents == 0L) 100.0 else 100.0 * presents / (presents + absents),
                presents, absents, cancels, requiredPercentage
            )
        }).asFlow().mapToOne(Dispatchers.IO)
    }

    fun markAttendanceForScheduleClass(
        scheduleId: Long,
        date: LocalDate,
        classStatus: CourseClassStatus
    ) {
        queries.markAttendance(scheduleId, date, classStatus)
    }

    fun markAttendanceForExtraClass(
        extraClassId: Long,
        status: CourseClassStatus
    ) = queries.updateExtraClassStatus(extraClassId = extraClassId, status = status)


    fun getScheduleClassesForCourse(courseId: Long) = queries.getScheduleClassesForCourse(
        courseId,
        mapper = { scheduleId, _, weekday, startTime, endTime, _ ->
            ClassDetail(weekday, startTime, endTime, scheduleId)
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

    fun getMarkedAttendancesForCourse(courseId: Long): Flow<List<MarkedAttendancesForCourse>> {
        return queries.markedAttendancesForCourse(courseId).asFlow().mapToList(Dispatchers.IO)
    }

    companion object {
        @Volatile
        private var instance: DBOps? = null

        fun getInstance(context: Context): DBOps {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = DBOps(getAndroidSqliteDriver(context))
                    }
                }
            }
            return instance!!
        }
    }


}

data class AttendanceCounts(
    val percent: Double,
    val present: Long,
    val absents: Long,
    val cancels: Long,
    val requiredPercentage: Double
)