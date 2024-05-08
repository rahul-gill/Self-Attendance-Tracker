package com.github.rahul_gill.attendance.db


import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.rahul_gill.attendance.Database
import com.github.rahul_gill.attendance.prefs.UnsetClassesBehavior
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

@RunWith(JUnit4::class)
class DBMigrationTest {

    private lateinit var driver: SqlDriver
    private lateinit var dbOps: DBOps

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        dbOps = DBOps(driver, unsetClassesBehavior = UnsetClassesBehavior.None)
    }

    @Test
    fun `check v1 to v2 migration`() {
        runBlocking {
            initV1Schema()
            //initV1FakeData
            courses.forEach {
                dbOps.createCourse(
                    name = it.courseName,
                    requiredAttendancePercentage = it.requiredAttendance,
                    schedule = listOf(
                        ClassDetail(
                            dayOfWeek = DayOfWeek.MONDAY,
                        ),
                        ClassDetail(
                            dayOfWeek = DayOfWeek.WEDNESDAY,
                        )
                    )
                )
            }
            runBlocking {
                dbOps.getCoursesDetailsList().firstOrNull()?.let { courses ->
                    courses.forEachIndexed { courseIndex, course ->
                        val schedule = dbOps.getScheduleClassesForCourse(course.courseId).first()
                        val dateX = LocalDate.now()
                            .with(TemporalAdjusters.firstInMonth(schedule.first().dayOfWeek))
                        for (i in 0..10) {
                            markAttendanceForScheduleClassV1(
                                classStatus = if (course.courseName == "Chemistry" && (i == 2 || i == 4))
                                    CourseClassStatus.Absent
                                else CourseClassStatus.Present,
                                scheduleId = schedule.first().scheduleId,
                                date = dateX.minusWeeks(i.toLong()),
                                courseId = course.courseId
                            )
                        }
                    }
                }
            }

            dbOps.getCoursesDetailsList().first().forEach { item ->
                println("For course: ${item.courseName} ${item.courseId}")
                dbOps.getScheduleClassesForCourse(item.courseId).first().forEach {
                    println("\n  Class: ${it.dayOfWeek} ${it.startTime} ${it.endTime}")
                }
            }
            //now v2

            Database.Schema.migrate(
                driver = driver,
                oldVersion = 1,
                newVersion = 2
            )
            println("\n\n\n\nNow comes v2")
            dbOps.getCoursesDetailsList().first().forEach { item ->
                println("For course: ${item.courseName} ${item.courseId}")
                dbOps.getScheduleClassesForCourse(item.courseId).first().forEach {
                    println("\n  Class: ${it.dayOfWeek} ${it.startTime} ${it.endTime}")
                }
            }
            println("\n\n\n\n\n\n")
            runBlocking {
                val mp =
                    mutableMapOf<Long, MutableMap<Long, MutableList<Pair<CourseClassStatus, LocalDate>>>>()
                val realCourseList = dbOps.getCoursesDetailsList().first()
                realCourseList.forEach { course ->
                    mp[course.courseId] = mutableMapOf()
                    dbOps.getMarkedAttendancesForCourse(course.courseId).first().forEach {
                        val rec = it as AttendanceRecordHybrid.ScheduledClass
                        if (mp[course.courseId]!![rec.scheduleId!!] == null) {
                            mp[course.courseId]!![rec.scheduleId!!] = mutableListOf()
                        }
                        mp[rec.courseId]!![rec.scheduleId!!]!!.add(Pair(rec.classStatus, rec.date))
                    }
                }
                courses.forEachIndexed { _, courseFacde ->
                    val course = realCourseList.find { it.courseName == courseFacde.courseName }!!
                    val schedule = dbOps.getScheduleClassesForCourse(course.courseId).first()
                    println("For course: ${course.courseName} ${course.courseId}")
                    schedule.forEach {
                        println("\n  Class: ${it.dayOfWeek} ${it.startTime} ${it.endTime}")
                    }

                    val dateX = LocalDate.now()
                        .with(TemporalAdjusters.firstInMonth(schedule.first().dayOfWeek))
                    for (i in 0..10) {
                        val rec =
                            mp[course.courseId]!![schedule.first().scheduleId!!]!!
                        assertTrue(
                            rec.find {
                                it.first == (if (course.courseName == "Chemistry" && (i == 2 || i == 4))
                                    CourseClassStatus.Absent else CourseClassStatus.Present) && it.second == dateX.minusWeeks(
                                    i.toLong()
                                )
                            } != null
                        )
                    }
                }
            }


        }
    }

    private fun initV1Schema() {
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE Course (\n" +
                    "    courseId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    courseName TEXT NOT NULL,\n" +
                    "    requiredAttendance REAL NOT NULL\n" +
                    ")",
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE Schedule (\n" +
                    "    scheduleId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    courseId INTEGER NOT NULL,\n" +
                    "    weekday INTEGER  NOT NULL,\n" +
                    "    startTime TEXT   NOT NULL,\n" +
                    "    endTime TEXT   NOT NULL,\n" +
                    "    includedInSchedule INTEGER NOT NULL DEFAULT 0,\n" +
                    "    CONSTRAINT fk_course\n" +
                    "        FOREIGN KEY (courseId)\n" +
                    "        REFERENCES Course (courseId)\n" +
                    "        ON DELETE CASCADE\n" +
                    ")",
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE Attendance (\n" +
                    "    scheduleId INTEGER NOT NULL,\n" +
                    "    classStatus TEXT NOT NULL,\n" +
                    "    date TEXT  NOT NULL,\n" +
                    "    CONSTRAINT fk_schedule\n" +
                    "        FOREIGN KEY (scheduleId)\n" +
                    "        REFERENCES Schedule (scheduleId)\n" +
                    "        ON DELETE CASCADE,\n" +
                    "    CONSTRAINT pk_attendance\n" +
                    "        PRIMARY KEY (scheduleId, date)\n" +
                    ")",
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = "CREATE TABLE ExtraClasses (\n" +
                    "    extraClassId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    courseId INTEGER NOT NULL,\n" +
                    "    date TEXT  NOT NULL,\n" +
                    "    startTime TEXT  NOT NULL,\n" +
                    "    endTime TEXT  NOT NULL,\n" +
                    "    classStatus TEXT  NOT NULL,\n" +
                    "    CONSTRAINT fk_course\n" +
                    "        FOREIGN KEY (courseId)\n" +
                    "        REFERENCES Course (courseId)\n" +
                    "        ON DELETE CASCADE\n" +
                    ")",
            parameters = 0
        )
    }


    private fun markAttendanceForScheduleClassV1(
        classStatus: CourseClassStatus,
        scheduleId: Long?,
        date: LocalDate,
        courseId: Long
    ) {
        driver.execute(
            identifier = null,
            sql = "INSERT  INTO Attendance (classStatus, scheduleId, date) " +
                    "VALUES ( ?, ?, ?);",
            parameters = 4,
            binders = {
                bindString(0, classStatus.toString())
                bindLong(1, scheduleId)
                bindString(2, LocalDateAdapter.encode(date))
            }
        )
    }

    private val courses = listOf(
        "Physics",
        "Chemistry",
        "Social",
        "Fighting lab",
        "French",
        "Mathematics",
        "English",
        "Politics"
    ).mapIndexed { indx, name ->
        val presents = 10
        val absents = if (indx == 4) 2 else 0
        val cancels = 0
        CourseDetailsOverallItem(
            courseId = indx.toLong(),
            courseName = name,
            requiredAttendance = 75.0,
            currentAttendancePercentage = 100.0 * presents / (presents + absents),
            presents = presents.toInt(),
            absents = absents.toInt(),
            cancels = cancels.toInt()
        )
    }
    private val demoTodayClasses: List<Pair<AttendanceRecordHybrid, AttendanceCounts>>
        get() {
            val date = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            var startTime = LocalTime.of(10, 0)
            val endTime = { startTime.plusHours(1) }
            val list = mutableListOf<AttendanceRecordHybrid>()

            for (i in 0..7) {
                if (i == 5) {
                    AttendanceRecordHybrid.ExtraClass(
                        extraClassId = i.toLong(),
                        startTime = startTime,
                        endTime = endTime(),
                        courseName = courses[i].courseName,
                        date = date,
                        classStatus = CourseClassStatus.Present,
                        courseId = i.toLong()
                    )
                } else {
                    list.add(
                        AttendanceRecordHybrid.ScheduledClass(
                            attendanceId = i.toLong(),
                            scheduleId = i.toLong(),
                            startTime = startTime,
                            endTime = endTime(),
                            courseName = courses[i].courseName,
                            date = date,
                            classStatus = if (i == 2) CourseClassStatus.Absent else CourseClassStatus.Present,
                            courseId = i.toLong()
                        )
                    )
                }
                startTime = startTime.plusHours(1)
            }


            return list.map { record ->
                Pair(
                    record,
                    courses.find { it.courseId == record.courseId }!!.run {
                        val presentsLater =
                            (presents).toLong()
                        val absentsLater =
                            (absents).toLong()
                        AttendanceCounts(
                            if (absentsLater + presentsLater == 0L) 100.0 else 100.0 * presentsLater / (absentsLater + presentsLater),
                            presentsLater,
                            absentsLater,
                            cancels.toLong(),
                            unsets.toLong(),
                            requiredAttendance
                        )
                    }
                )
            }
        }
}