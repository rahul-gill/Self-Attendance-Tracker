package com.github.rahul_gill.attendance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L)
        val courseName = intent.getStringExtra(EXTRA_COURSE_NAME) ?: return
        val startTimeStr = intent.getStringExtra(EXTRA_START_TIME) ?: return
        val endTimeStr = intent.getStringExtra(EXTRA_END_TIME) ?: return
        val dateStr = intent.getStringExtra(EXTRA_DATE) ?: LocalDate.now().toString()

        if (scheduleId == -1L || courseId == -1L) {
            Timber.e("AlarmReceiver: missing scheduleId or courseId")
            return
        }

        val startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ISO_TIME)
        val endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ISO_TIME)
        val date = LocalDate.parse(dateStr)

        Timber.d("AlarmReceiver: showing notification for $courseName at $startTime")

        NotificationHelper.showClassReminderNotification(
            context = context,
            scheduleId = scheduleId,
            courseId = courseId,
            courseName = courseName,
            startTime = startTime,
            endTime = endTime,
            date = date
        )
    }

    companion object {
        const val EXTRA_SCHEDULE_ID = "alarm_schedule_id"
        const val EXTRA_COURSE_ID = "alarm_course_id"
        const val EXTRA_COURSE_NAME = "alarm_course_name"
        const val EXTRA_START_TIME = "alarm_start_time"
        const val EXTRA_END_TIME = "alarm_end_time"
        const val EXTRA_DATE = "alarm_date"
    }
}
