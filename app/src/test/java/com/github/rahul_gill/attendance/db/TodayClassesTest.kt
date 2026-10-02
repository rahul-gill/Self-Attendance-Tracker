package com.github.rahul_gill.attendance.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.rahul_gill.attendance.Database
import com.github.rahul_gill.attendance.prefs.UnsetClassesBehavior
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDate
import java.time.LocalTime

@RunWith(JUnit4::class)
class TodayClassesTest {

    private lateinit var driver: SqlDriver
    private lateinit var dbOps: DBOps

    private val today = LocalDate.now()

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        dbOps = DBOps(driver, unsetClassesBehavior = UnsetClassesBehavior.None)
    }

    private fun todayClasses() = runBlocking {
        dbOps.getScheduleAndExtraClassesForToday().first().map { it.first }
    }

    private fun classToday(startHour: Int) = ClassDetail(
        dayOfWeek = today.dayOfWeek,
        startTime = LocalTime.of(startHour, 0),
        endTime = LocalTime.of(startHour + 1, 0)
    )

    @Test
    fun `today's classes are sorted by earliest start time`() {
        val physicsId = dbOps.createCourse(
            name = "Physics",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(classToday(14))
        )
        dbOps.createCourse(
            name = "Maths",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(classToday(9))
        )
        dbOps.createExtraClasses(
            courseId = physicsId,
            timings = ExtraClassTimings(
                date = today,
                startTime = LocalTime.of(11, 0),
                endTime = LocalTime.of(12, 0)
            )
        )

        val classes = todayClasses()

        assertEquals(
            listOf(LocalTime.of(9, 0), LocalTime.of(11, 0), LocalTime.of(14, 0)),
            classes.map { it.startTime }
        )
        assertEquals(listOf("Maths", "Physics", "Physics"), classes.map { it.courseName })
    }

    @Test
    fun `classes excluded from schedule are not listed for today`() = runBlocking {
        val courseId = dbOps.createCourse(
            name = "Chemistry",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(classToday(9), classToday(11))
        )
        val excluded = dbOps.getScheduleClassesForCourse(courseId).first()
            .first { it.startTime == LocalTime.of(11, 0) }
        dbOps.changeActivateStatusOfScheduleItem(excluded.scheduleId!!, activate = false)

        assertEquals(listOf(LocalTime.of(9, 0)), todayClasses().map { it.startTime })
    }

    @Test
    fun `classes marked today stay listed with their status`() = runBlocking {
        val courseId = dbOps.createCourse(
            name = "Biology",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(classToday(9), classToday(11))
        )
        val marked = dbOps.getScheduleClassesForCourse(courseId).first()
            .first { it.startTime == LocalTime.of(9, 0) }
        dbOps.markAttendanceForScheduleClass(
            attendanceId = null,
            classStatus = CourseClassStatus.Present,
            scheduleId = marked.scheduleId,
            date = today,
            courseId = courseId
        )

        val classes = todayClasses()

        assertEquals(
            listOf(CourseClassStatus.Present, CourseClassStatus.Unset),
            classes.map { it.classStatus }
        )
    }

    // Guards against matching the weekday in UTC instead of local time; that
    // regression only shows when the local date differs from the UTC date.
    @Test
    fun `today's classes are picked by the local weekday`() {
        dbOps.createCourse(
            name = "History",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(
                classToday(9),
                classToday(10).copy(dayOfWeek = today.dayOfWeek.minus(1)),
                classToday(11).copy(dayOfWeek = today.dayOfWeek.plus(1))
            )
        )

        assertEquals(listOf(LocalTime.of(9, 0)), todayClasses().map { it.startTime })
    }
}
