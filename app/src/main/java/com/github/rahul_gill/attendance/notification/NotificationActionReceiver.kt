package com.github.rahul_gill.attendance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.DBOps
import timber.log.Timber
import java.time.LocalDate

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L)
        val dateStr = intent.getStringExtra(EXTRA_DATE) ?: return
        val statusStr = intent.getStringExtra(EXTRA_STATUS) ?: return

        if (scheduleId == -1L || courseId == -1L) {
            Timber.e("NotificationActionReceiver: missing scheduleId or courseId")
            return
        }

        val date = LocalDate.parse(dateStr)
        val status = CourseClassStatus.fromString(statusStr)

        Timber.d("NotificationActionReceiver: marking $statusStr for scheduleId=$scheduleId, courseId=$courseId, date=$date")

        try {
            DBOps.instance.markAttendanceForScheduleClass(
                attendanceId = null,
                classStatus = status,
                scheduleId = scheduleId,
                date = date,
                courseId = courseId
            )
            Timber.d("NotificationActionReceiver: attendance marked successfully")
        } catch (e: Exception) {
            Timber.e(e, "NotificationActionReceiver: failed to mark attendance")
        }

        if (notificationId != -1) {
            NotificationHelper.dismissNotification(context, notificationId)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_STATUS = "extra_status"
    }
}
