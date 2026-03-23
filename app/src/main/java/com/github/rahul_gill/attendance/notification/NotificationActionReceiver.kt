package com.github.rahul_gill.attendance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.rahul_gill.attendance.db.DBOps
import timber.log.Timber

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NotificationActionData.EXTRA_KEY, NotificationActionData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NotificationActionData.EXTRA_KEY)
        }

        if (data == null) {
            Timber.e("NotificationActionReceiver: missing NotificationActionData")
            return
        }

        Timber.d("NotificationActionReceiver: marking ${data.status} for scheduleId=${data.scheduleId}, courseId=${data.courseId}, date=${data.date}")

        try {
            DBOps.instance.markAttendanceForScheduleClass(
                attendanceId = null,
                classStatus = data.status,
                scheduleId = data.scheduleId,
                date = data.date,
                courseId = data.courseId
            )
            Timber.d("NotificationActionReceiver: attendance marked successfully")
        } catch (e: Exception) {
            Timber.e(e, "NotificationActionReceiver: failed to mark attendance")
        }

        if (data.notificationId != -1) {
            NotificationHelper.dismissNotification(context, data.notificationId)
        }
    }
}
