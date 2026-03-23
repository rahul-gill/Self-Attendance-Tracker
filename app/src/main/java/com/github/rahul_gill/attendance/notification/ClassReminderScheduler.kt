package com.github.rahul_gill.attendance.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ClassReminderScheduler {

    private const val REMINDER_MINUTES_BEFORE = 5L

    fun scheduleAlarmsForToday(context: Context) {
        val notificationsEnabled = PreferenceManager.notificationsEnabled.value
        
        if (!notificationsEnabled) {
            Timber.d("ClassReminderScheduler: notifications disabled, skipping")
            return
        }

        val today = LocalDate.now()
        val todayWeekday = today.dayOfWeek

        val classes = try {
            DBOps.instance.getActiveScheduleClassesForWeekday(todayWeekday)
        } catch (e: Exception) {
            Timber.e(e, "ClassReminderScheduler: failed to query schedule classes")
            return
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val now = LocalTime.now()

        Timber.d("ClassReminderScheduler: found ${classes.size} classes for $todayWeekday")

        for (classItem in classes) {
            val reminderTime = classItem.startTime.minusMinutes(REMINDER_MINUTES_BEFORE)

            if (reminderTime.isBefore(now)) {
                Timber.d("ClassReminderScheduler: skipping ${classItem.courseName} — reminder time $reminderTime already passed")
                continue
            }

            val triggerAtMillis = reminderTime
                .atDate(today)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val data = ClassReminderData(
                scheduleId = classItem.scheduleId,
                courseId = classItem.courseId,
                courseName = classItem.courseName,
                startTime = classItem.startTime,
                endTime = classItem.endTime,
                date = today
            )

            val pendingIntent = createAlarmPendingIntent(context, data)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                        Timber.w("ClassReminderScheduler: exact alarm permission not granted, using inexact alarm")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                Timber.d("ClassReminderScheduler: scheduled alarm for ${classItem.courseName} at $reminderTime")
            } catch (e: SecurityException) {
                Timber.e(e, "ClassReminderScheduler: SecurityException scheduling reminder alarm")
            }

        }
    }

    fun cancelAllAlarms(context: Context) {
        val today = LocalDate.now()
        val classes = try {
            DBOps.instance.getActiveScheduleClassesForWeekday(today.dayOfWeek)
        } catch (e: Exception) {
            Timber.e(e, "ClassReminderScheduler: failed to query for cancellation")
            return
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        for (classItem in classes) {
            val data = ClassReminderData(
                scheduleId = classItem.scheduleId,
                courseId = classItem.courseId,
                courseName = classItem.courseName,
                startTime = classItem.startTime,
                endTime = classItem.endTime,
                date = today
            )
            val pendingIntent = createAlarmPendingIntent(context, data)
            alarmManager.cancel(pendingIntent)
        }
        Timber.d("ClassReminderScheduler: cancelled all alarms")
    }

    private fun createAlarmPendingIntent(
        context: Context,
        data: ClassReminderData
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(ClassReminderData.EXTRA_KEY, data)
        }
        return PendingIntent.getBroadcast(
            context,
            data.scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
