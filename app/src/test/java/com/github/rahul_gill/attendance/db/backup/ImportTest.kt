package com.github.rahul_gill.attendance.db.backup

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
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
class ImportTest {

    private lateinit var driver: SqlDriver
    private lateinit var dbOps: DBOps

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        dbOps = DBOps(driver, unsetClassesBehavior = UnsetClassesBehavior.None)
    }

    @Test
    fun `import empty courses array clears existing data`() = runBlocking {
        // Add a course first
        dbOps.createCourse(
            name = "TempCourse",
            requiredAttendancePercentage = 50.0,
            schedule = listOf(ClassDetail(dayOfWeek = DayOfWeek.MONDAY))
        )

        val emptyJson = """
            {
                "version": 1,
                "exportDate": "2026-03-26",
                "courses": []
            }
        """.trimIndent()

        BackupManager.importFromJson(dbOps, emptyJson)

        val courses = dbOps.getCoursesDetailsList().first()
        assertTrue(courses.isEmpty())
    }

    @Test
    fun `import single course with schedule`() = runBlocking {
        val json = """
            {
                "version": 1,
                "exportDate": "2026-03-26",
                "courses": [
                    {
                        "courseName": "Mathematics",
                        "requiredAttendance": 75.0,
                        "schedules": [
                            {
                                "weekday": "MONDAY",
                                "startTime": "09:00:00",
                                "endTime": "10:00:00",
                                "includedInSchedule": true
                            },
                            {
                                "weekday": "WEDNESDAY",
                                "startTime": "11:00:00",
                                "endTime": "12:00:00",
                                "includedInSchedule": true
                            }
                        ],
                        "attendanceRecords": [],
                        "extraClasses": []
                    }
                ]
            }
        """.trimIndent()

        BackupManager.importFromJson(dbOps, json)

        val courses = dbOps.getCoursesDetailsList().first()
        assertEquals(1, courses.size)
        assertEquals("Mathematics", courses[0].courseName)
        assertEquals(75.0, courses[0].requiredAttendance, 0.01)

        val schedules = dbOps.getScheduleClassesForCourse(courses[0].courseId).first()
        assertEquals(2, schedules.size)
        assertEquals(DayOfWeek.MONDAY, schedules[0].dayOfWeek)
        assertEquals(DayOfWeek.WEDNESDAY, schedules[1].dayOfWeek)
    }

    @Test
    fun `import with attendance records restores them correctly`() = runBlocking {
        val json = """
            {
                "version": 1,
                "exportDate": "2026-03-26",
                "courses": [
                    {
                        "courseName": "Physics",
                        "requiredAttendance": 80.0,
                        "schedules": [
                            {
                                "weekday": "TUESDAY",
                                "startTime": "14:00:00",
                                "endTime": "15:00:00",
                                "includedInSchedule": true
                            }
                        ],
                        "attendanceRecords": [
                            {
                                "classStatus": "Present",
                                "date": "2026-03-24",
                                "scheduleWeekday": "TUESDAY",
                                "scheduleStartTime": "14:00:00"
                            },
                            {
                                "classStatus": "Absent",
                                "date": "2026-03-17",
                                "scheduleWeekday": "TUESDAY",
                                "scheduleStartTime": "14:00:00"
                            }
                        ],
                        "extraClasses": []
                    }
                ]
            }
        """.trimIndent()

        BackupManager.importFromJson(dbOps, json)

        val courses = dbOps.getCoursesDetailsList().first()
        assertEquals(1, courses.size)
        assertEquals("Physics", courses[0].courseName)
        // 1 present, 1 absent
        assertEquals(1, courses[0].presents)
        assertEquals(1, courses[0].absents)
    }

    @Test
    fun `import with extra classes`() = runBlocking {
        val json = """
            {
                "version": 1,
                "exportDate": "2026-03-26",
                "courses": [
                    {
                        "courseName": "Chemistry",
                        "requiredAttendance": 70.0,
                        "schedules": [
                            {
                                "weekday": "FRIDAY",
                                "startTime": "10:00:00",
                                "endTime": "11:00:00",
                                "includedInSchedule": true
                            }
                        ],
                        "attendanceRecords": [],
                        "extraClasses": [
                            {
                                "date": "2026-03-28",
                                "startTime": "15:00:00",
                                "endTime": "16:00:00",
                                "classStatus": "Present"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        BackupManager.importFromJson(dbOps, json)

        val courses = dbOps.getCoursesDetailsList().first()
        assertEquals(1, courses.size)
        // 1 present from extra class
        assertEquals(1, courses[0].presents)
    }

    @Test
    fun `import replaces existing data`() = runBlocking {
        // Add original data
        dbOps.createCourse(
            name = "OldCourse",
            requiredAttendancePercentage = 50.0,
            schedule = listOf(ClassDetail(dayOfWeek = DayOfWeek.MONDAY))
        )

        assertEquals(1, dbOps.getCoursesDetailsList().first().size)

        // Import new data
        val json = """
            {
                "version": 1,
                "exportDate": "2026-03-26",
                "courses": [
                    {
                        "courseName": "NewCourse1",
                        "requiredAttendance": 80.0,
                        "schedules": [{ "weekday": "TUESDAY", "startTime": "09:00:00", "endTime": "10:00:00", "includedInSchedule": true }],
                        "attendanceRecords": [],
                        "extraClasses": []
                    },
                    {
                        "courseName": "NewCourse2",
                        "requiredAttendance": 60.0,
                        "schedules": [{ "weekday": "THURSDAY", "startTime": "14:00:00", "endTime": "15:00:00", "includedInSchedule": true }],
                        "attendanceRecords": [],
                        "extraClasses": []
                    }
                ]
            }
        """.trimIndent()

        BackupManager.importFromJson(dbOps, json)

        val courses = dbOps.getCoursesDetailsList().first()
        assertEquals(2, courses.size)
        assertTrue(courses.none { it.courseName == "OldCourse" })
        assertTrue(courses.any { it.courseName == "NewCourse1" })
        assertTrue(courses.any { it.courseName == "NewCourse2" })
    }

    @Test
    fun `export then import roundtrip preserves all data`() = runBlocking {
        // Create complex data
        val courseId = dbOps.createCourse(
            name = "Biology",
            requiredAttendancePercentage = 65.0,
            schedule = listOf(
                ClassDetail(
                    dayOfWeek = DayOfWeek.MONDAY,
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(9, 0)
                ),
                ClassDetail(
                    dayOfWeek = DayOfWeek.THURSDAY,
                    startTime = LocalTime.of(13, 0),
                    endTime = LocalTime.of(14, 0)
                )
            )
        )

        // Mark some attendance
        val schedules = dbOps.getScheduleClassesForCourse(courseId).first()
        dbOps.markAttendanceForScheduleClass(
            attendanceId = null,
            classStatus = CourseClassStatus.Present,
            scheduleId = schedules[0].scheduleId,
            date = LocalDate.of(2026, 3, 23),
            courseId = courseId
        )
        dbOps.markAttendanceForScheduleClass(
            attendanceId = null,
            classStatus = CourseClassStatus.Absent,
            scheduleId = schedules[0].scheduleId,
            date = LocalDate.of(2026, 3, 16),
            courseId = courseId
        )

        // Add extra class
        dbOps.createExtraClasses(
            courseId = courseId,
            timings = ExtraClassTimings(
                date = LocalDate.of(2026, 3, 25),
                startTime = LocalTime.of(16, 0),
                endTime = LocalTime.of(17, 0)
            )
        )

        // Export
        val exportedJson = BackupManager.exportToJson(dbOps)

        // Clear and reimport into same DB
        BackupManager.importFromJson(dbOps, exportedJson)

        // Verify
        val courses = dbOps.getCoursesDetailsList().first()
        assertEquals(1, courses.size)
        assertEquals("Biology", courses[0].courseName)
        assertEquals(65.0, courses[0].requiredAttendance, 0.01)
        assertEquals(1, courses[0].presents) // 1 present (schedule) — extra class is Unset
        assertEquals(1, courses[0].absents)

        val restoredSchedules = dbOps.getScheduleClassesForCourse(courses[0].courseId).first()
        assertEquals(2, restoredSchedules.size)

        val extraClasses = dbOps.getExtraClassesListForCourse(courses[0].courseId).first()
        assertEquals(1, extraClasses.size)
        assertEquals(LocalDate.of(2026, 3, 25), extraClasses[0].date)
    }
}
