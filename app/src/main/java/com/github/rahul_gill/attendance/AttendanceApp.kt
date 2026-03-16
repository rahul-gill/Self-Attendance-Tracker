package com.github.rahul_gill.attendance

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.github.rahul_gill.attendance.notification.ClassReminderScheduler
import com.github.rahul_gill.attendance.notification.DailySchedulerWorker
import com.github.rahul_gill.attendance.notification.NotificationHelper
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import timber.log.Timber


class AttendanceApp : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // If notifications are enabled, schedule alarms for today and enqueue daily worker
        if (PreferenceManager.notificationsEnabled.value) {
            ClassReminderScheduler.scheduleAlarmsForToday(this)
            DailySchedulerWorker.enqueue(this)
        }
    }

}

