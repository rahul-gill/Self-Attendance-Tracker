package com.github.rahul_gill.attendance.db

import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Handles export and import of all course data as JSON.
 * Uses [org.json] (Android SDK) so no extra dependencies are needed.
 */
object BackupManager {

    private const val BACKUP_VERSION = 1

    /**
     * Export all data from the database as a JSON string.
     */
    fun exportToJson(dbOps: DBOps): String {
        val queries = dbOps.db.appQueries
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("exportDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE))

        val coursesArray = JSONArray()
        val allCourses = queries.getAllCourses().executeAsList()

        for (course in allCourses) {
            val courseObj = JSONObject()
            courseObj.put("courseName", course.courseName)
            courseObj.put("requiredAttendance", course.requiredAttendance)

            // Schedules
            val schedulesArray = JSONArray()
            val schedules = queries.getAllSchedulesForCourse(course.courseId).executeAsList()
            for (schedule in schedules) {
                val schedObj = JSONObject()
                schedObj.put("weekday", schedule.weekday.name)
                schedObj.put("startTime", schedule.startTime.format(DateTimeFormatter.ISO_TIME))
                schedObj.put("endTime", schedule.endTime.format(DateTimeFormatter.ISO_TIME))
                schedObj.put("includedInSchedule", schedule.includedInSchedule != 0L)
                schedulesArray.put(schedObj)
            }
            courseObj.put("schedules", schedulesArray)

            // Attendance records
            val attendanceArray = JSONArray()
            val attendanceRecords = queries.getAllAttendanceForCourse(course.courseId).executeAsList()
            for (att in attendanceRecords) {
                val attObj = JSONObject()
                attObj.put("classStatus", att.classStatus.name)
                attObj.put("date", att.date.format(DateTimeFormatter.ISO_DATE))
                // Find the matching schedule to store weekday+time instead of scheduleId
                if (att.scheduleId != null) {
                    val matchingSchedule = schedules.find { it.scheduleId == att.scheduleId }
                    if (matchingSchedule != null) {
                        attObj.put("scheduleWeekday", matchingSchedule.weekday.name)
                        attObj.put("scheduleStartTime", matchingSchedule.startTime.format(DateTimeFormatter.ISO_TIME))
                    }
                }
                attendanceArray.put(attObj)
            }
            courseObj.put("attendanceRecords", attendanceArray)

            // Extra classes
            val extraClassesArray = JSONArray()
            val extraClasses = queries.getAllExtraClassesForCourse(course.courseId).executeAsList()
            for (extra in extraClasses) {
                val extraObj = JSONObject()
                extraObj.put("date", extra.date.format(DateTimeFormatter.ISO_DATE))
                extraObj.put("startTime", extra.startTime.format(DateTimeFormatter.ISO_TIME))
                extraObj.put("endTime", extra.endTime.format(DateTimeFormatter.ISO_TIME))
                extraObj.put("classStatus", extra.classStatus.name)
                extraClassesArray.put(extraObj)
            }
            courseObj.put("extraClasses", extraClassesArray)

            coursesArray.put(courseObj)
        }

        root.put("courses", coursesArray)
        return root.toString(2)
    }

    /**
     * Import data from a JSON string. Replaces all existing data.
     */
    fun importFromJson(dbOps: DBOps, jsonString: String) {
        val root = JSONObject(jsonString)
        val coursesArray = root.getJSONArray("courses")
        val queries = dbOps.db.appQueries

        dbOps.db.transaction {
            // Clear all existing data (CASCADE deletes schedules, attendance, extra classes)
            queries.deleteAllData()

            for (i in 0 until coursesArray.length()) {
                val courseObj = coursesArray.getJSONObject(i)
                val courseName = courseObj.getString("courseName")
                val requiredAttendance = courseObj.getDouble("requiredAttendance")

                queries.createCourse(courseName, requiredAttendance)
                val courseId = queries.getLastInsertRowID().executeAsOne()

                // Import schedules
                val schedulesArray = courseObj.getJSONArray("schedules")
                // Map of (weekday, startTime) -> scheduleId for attendance linking
                val scheduleIdMap = mutableMapOf<String, Long>()

                for (j in 0 until schedulesArray.length()) {
                    val schedObj = schedulesArray.getJSONObject(j)
                    val weekday = DayOfWeek.valueOf(schedObj.getString("weekday"))
                    val startTime = LocalTime.parse(schedObj.getString("startTime"))
                    val endTime = LocalTime.parse(schedObj.getString("endTime"))
                    val included = if (schedObj.optBoolean("includedInSchedule", true)) 1L else 0L

                    queries.createScheduleItemForCourse(courseId, weekday, startTime, endTime, included)
                    val scheduleId = queries.getLastInsertRowID().executeAsOne()
                    val key = "${weekday.name}|${startTime.format(DateTimeFormatter.ISO_TIME)}"
                    scheduleIdMap[key] = scheduleId
                }

                // Import attendance records
                val attendanceArray = courseObj.getJSONArray("attendanceRecords")
                for (j in 0 until attendanceArray.length()) {
                    val attObj = attendanceArray.getJSONObject(j)
                    val status = CourseClassStatus.fromString(attObj.getString("classStatus"))
                    val date = LocalDate.parse(attObj.getString("date"))

                    // Resolve scheduleId from weekday+startTime
                    var scheduleId: Long? = null
                    if (attObj.has("scheduleWeekday") && attObj.has("scheduleStartTime")) {
                        val key = "${attObj.getString("scheduleWeekday")}|${attObj.getString("scheduleStartTime")}"
                        scheduleId = scheduleIdMap[key]
                    }

                    queries.markAttendanceInsert(status, scheduleId, date, courseId)
                }

                // Import extra classes
                val extraClassesArray = courseObj.getJSONArray("extraClasses")
                for (j in 0 until extraClassesArray.length()) {
                    val extraObj = extraClassesArray.getJSONObject(j)
                    val date = LocalDate.parse(extraObj.getString("date"))
                    val startTime = LocalTime.parse(extraObj.getString("startTime"))
                    val endTime = LocalTime.parse(extraObj.getString("endTime"))
                    val status = CourseClassStatus.fromString(extraObj.getString("classStatus"))

                    queries.createExtraClass(courseId, date, startTime, endTime, status)
                }
            }
        }
    }
}
