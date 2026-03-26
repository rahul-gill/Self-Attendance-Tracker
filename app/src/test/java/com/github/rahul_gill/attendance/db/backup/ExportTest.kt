package com.github.rahul_gill.attendance.db.backup

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.rahul_gill.attendance.Database
import com.github.rahul_gill.attendance.db.BackupManager
import com.github.rahul_gill.attendance.db.ClassDetail
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.ExtraClassTimings
import com.github.rahul_gill.attendance.prefs.UnsetClassesBehavior
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@RunWith(JUnit4::class)
class ExportTest {

    private lateinit var driver: SqlDriver
    private lateinit var dbOps: DBOps

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        dbOps = DBOps(driver, unsetClassesBehavior = UnsetClassesBehavior.None)
    }

    @Test
    fun `export empty database produces valid JSON with empty courses`() {
        val json = BackupManager.exportToJson(dbOps)
        val root = JSONObject(json)

        assertEquals(1, root.getInt("version"))
        assertTrue(root.has("exportDate"))
        assertEquals(0, root.getJSONArray("courses").length())
    }

    @Test
    fun `export single course with schedule`() {
        dbOps.createCourse(
            name = "Mathematics",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(
                ClassDetail(
                    dayOfWeek = DayOfWeek.MONDAY,
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(10, 0)
                ),
                ClassDetail(
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    startTime = LocalTime.of(11, 0),
                    endTime = LocalTime.of(12, 0)
                )
            )
        )

        val json = BackupManager.exportToJson(dbOps)
        val root = JSONObject(json)
        val courses = root.getJSONArray("courses")

        assertEquals(1, courses.length())

        val course = courses.getJSONObject(0)
        assertEquals("Mathematics", course.getString("courseName"))
        assertEquals(75.0, course.getDouble("requiredAttendance"), 0.01)

        val schedules = course.getJSONArray("schedules")
        assertEquals(2, schedules.length())
        assertEquals("MONDAY", schedules.getJSONObject(0).getString("weekday"))
        assertEquals("WEDNESDAY", schedules.getJSONObject(1).getString("weekday"))
    }

    @Test
    fun `export includes attendance records`() = runBlocking {
        val courseId = dbOps.createCourse(
            name = "Physics",
            requiredAttendancePercentage = 80.0,
            schedule = listOf(
                ClassDetail(
                    dayOfWeek = DayOfWeek.TUESDAY,
                    startTime = LocalTime.of(14, 0),
                    endTime = LocalTime.of(15, 0)
                )
            )
        )

        val schedules = dbOps.getScheduleClassesForCourse(courseId).first()
        val scheduleId = schedules.first().scheduleId

        dbOps.markAttendanceForScheduleClass(
            attendanceId = null,
            classStatus = CourseClassStatus.Present,
            scheduleId = scheduleId,
            date = LocalDate.of(2026, 3, 24),
            courseId = courseId
        )

        val json = BackupManager.exportToJson(dbOps)
        val root = JSONObject(json)
        val course = root.getJSONArray("courses").getJSONObject(0)
        val attendanceRecords = course.getJSONArray("attendanceRecords")

        assertEquals(1, attendanceRecords.length())
        val record = attendanceRecords.getJSONObject(0)
        assertEquals("Present", record.getString("classStatus"))
        assertEquals("2026-03-24", record.getString("date"))
        assertEquals("TUESDAY", record.getString("scheduleWeekday"))
    }

    @Test
    fun `export includes extra classes`() {
        val courseId = dbOps.createCourse(
            name = "Chemistry",
            requiredAttendancePercentage = 70.0,
            schedule = listOf(
                ClassDetail(
                    dayOfWeek = DayOfWeek.FRIDAY,
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(11, 0)
                )
            )
        )

        dbOps.createExtraClasses(
            courseId = courseId,
            timings = ExtraClassTimings(
                date = LocalDate.of(2026, 3, 28),
                startTime = LocalTime.of(15, 0),
                endTime = LocalTime.of(16, 0)
            )
        )

        val json = BackupManager.exportToJson(dbOps)
        val root = JSONObject(json)
        val course = root.getJSONArray("courses").getJSONObject(0)
        val extraClasses = course.getJSONArray("extraClasses")

        assertEquals(1, extraClasses.length())
        val extra = extraClasses.getJSONObject(0)
        assertEquals("2026-03-28", extra.getString("date"))
        assertEquals("Unset", extra.getString("classStatus"))
    }

    @Test
    fun `export multiple courses`() {
        dbOps.createCourse(
            name = "English",
            requiredAttendancePercentage = 75.0,
            schedule = listOf(ClassDetail(dayOfWeek = DayOfWeek.MONDAY))
        )
        dbOps.createCourse(
            name = "History",
            requiredAttendancePercentage = 60.0,
            schedule = listOf(ClassDetail(dayOfWeek = DayOfWeek.THURSDAY))
        )
        dbOps.createCourse(
            name = "Art",
            requiredAttendancePercentage = 50.0,
            schedule = listOf(ClassDetail(dayOfWeek = DayOfWeek.SATURDAY))
        )

        val json = BackupManager.exportToJson(dbOps)
        val root = JSONObject(json)
        assertEquals(3, root.getJSONArray("courses").length())
    }

    @Test
    fun `exported JSON is valid and parseable`() {
        dbOps.createCourse(
            name = "Science",
            requiredAttendancePercentage = 85.0,
            schedule = listOf(
                ClassDetail(dayOfWeek = DayOfWeek.MONDAY),
                ClassDetail(dayOfWeek = DayOfWeek.FRIDAY)
            )
        )

        val json = BackupManager.exportToJson(dbOps)

        // Should not throw
        val root = JSONObject(json)
        assertNotNull(root)
        assertTrue(json.contains("Science"))
        assertTrue(json.contains("85"))
    }
}
