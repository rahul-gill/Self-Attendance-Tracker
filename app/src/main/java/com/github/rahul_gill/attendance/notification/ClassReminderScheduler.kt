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
    private const val PREFS_NAME = "class_reminder_scheduler"
    private const val KEY_SCHEDULED_IDS = "scheduled_alarm_request_codes"

    fun scheduleAlarmsForToday(context: Context) {
        val notificationsEnabled = PreferenceManager.notificationsEnabled.value
        
        if (!notificationsEnabled) {
            Timber.d("ClassReminderScheduler: notifications disabled, skipping")
            return
        }

        // Cancel all previously scheduled alarms first to avoid stale alarms
        // (e.g. when a class is moved to a different day/time or deleted)
        cancelTrackedAlarms(context)

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
        val scheduledRequestCodes = mutableSetOf<Int>()

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
                scheduledRequestCodes.add(data.scheduleId.toInt())
                Timber.d("ClassReminderScheduler: scheduled alarm for ${classItem.courseName} at $reminderTime")
            } catch (e: SecurityException) {
                Timber.e(e, "ClassReminderScheduler: SecurityException scheduling reminder alarm")
            }

        }

        // Persist the request codes so we can cancel them later even if
        // the underlying schedule data has changed
        saveTrackedAlarmIds(context, scheduledRequestCodes)
    }

    fun cancelAllAlarms(context: Context) {
        cancelTrackedAlarms(context)
        Timber.d("ClassReminderScheduler: cancelled all alarms")
    }

    /**
     * Cancel all previously tracked alarms by their request codes.
     * This works even if the schedule data has since been changed or deleted,
     * because we use the saved request codes (not a fresh DB query).
     */
    private fun cancelTrackedAlarms(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCodes = prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet()) ?: emptySet()

        if (savedCodes.isEmpty()) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        for (codeStr in savedCodes) {
            val requestCode = codeStr.toIntOrNull() ?: continue
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Timber.d("ClassReminderScheduler: cancelled alarm with requestCode=$requestCode")
            }
        }

        // Clear the saved codes
        prefs.edit().remove(KEY_SCHEDULED_IDS).apply()
    }

    private fun saveTrackedAlarmIds(context: Context, requestCodes: Set<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet(KEY_SCHEDULED_IDS, requestCodes.map { it.toString() }.toSet())
            .apply()
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
