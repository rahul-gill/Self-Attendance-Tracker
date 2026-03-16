package com.github.rahul_gill.attendance.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.github.rahul_gill.attendance.MainActivity
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object NotificationHelper {

    const val CHANNEL_ID = "class_reminders"
    private const val NOTIFICATION_TAG = "class_reminder"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showClassReminderNotification(
        context: Context,
        scheduleId: Long,
        courseId: Long,
        courseName: String,
        startTime: LocalTime,
        endTime: LocalTime,
        date: LocalDate
    ) {
        val timeStr = startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        val endTimeStr = endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = scheduleId.toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_calendar_today_24)
            .setContentTitle(courseName)
            .setContentText(
                context.getString(R.string.notification_body, timeStr, endTimeStr)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.baseline_check_24,
                context.getString(R.string.mark_present),
                createActionPendingIntent(
                    context, notificationId, scheduleId, courseId, date,
                    CourseClassStatus.Present
                )
            )
            .addAction(
                R.drawable.baseline_close_24,
                context.getString(R.string.mark_absent),
                createActionPendingIntent(
                    context, notificationId, scheduleId, courseId, date,
                    CourseClassStatus.Absent
                )
            )
            .addAction(
                R.drawable.baseline_block_24,
                context.getString(R.string.mark_cancelled),
                createActionPendingIntent(
                    context, notificationId, scheduleId, courseId, date,
                    CourseClassStatus.Cancelled
                )
            )
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_TAG, notificationId, notification)
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_TAG, notificationId)
    }

    private fun createActionPendingIntent(
        context: Context,
        notificationId: Int,
        scheduleId: Long,
        courseId: Long,
        date: LocalDate,
        status: CourseClassStatus
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.github.rahul_gill.attendance.ACTION_MARK_${status.name.uppercase()}"
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(NotificationActionReceiver.EXTRA_COURSE_ID, courseId)
            putExtra(NotificationActionReceiver.EXTRA_DATE, date.toString())
            putExtra(NotificationActionReceiver.EXTRA_STATUS, status.name)
        }
        val requestCode = (scheduleId * 10 + status.ordinal).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
