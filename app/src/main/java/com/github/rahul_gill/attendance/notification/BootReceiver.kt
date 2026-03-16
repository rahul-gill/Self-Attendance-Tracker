package com.github.rahul_gill.attendance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Timber.d("BootReceiver: device booted, rescheduling alarms")

        if (PreferenceManager.notificationsEnabled.value) {
            ClassReminderScheduler.scheduleAlarmsForToday(context)
            DailySchedulerWorker.enqueue(context)
        }
    }
}
