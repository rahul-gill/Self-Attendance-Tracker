package com.github.rahul_gill.attendance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import timber.log.Timber

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ClassReminderData.EXTRA_KEY, ClassReminderData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(ClassReminderData.EXTRA_KEY)
        }

        if (data == null) {
            Timber.e("AlarmReceiver: missing ClassReminderData")
            return
        }

        Timber.d("AlarmReceiver: showing notification for ${data.courseName} at ${data.startTime}")

        NotificationHelper.showClassReminderNotification(context, data)
    }
}
