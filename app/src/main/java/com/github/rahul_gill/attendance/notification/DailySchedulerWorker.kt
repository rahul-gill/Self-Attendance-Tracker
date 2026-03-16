package com.github.rahul_gill.attendance.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DailySchedulerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("DailySchedulerWorker: scheduling alarms for today")
        return try {
            ClassReminderScheduler.scheduleAlarmsForToday(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DailySchedulerWorker: failed")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "daily_class_reminder_scheduler"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailySchedulerWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Timber.d("DailySchedulerWorker: enqueued periodic work")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("DailySchedulerWorker: cancelled periodic work")
        }
    }
}
