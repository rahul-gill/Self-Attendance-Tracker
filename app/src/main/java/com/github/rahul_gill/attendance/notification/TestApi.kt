package com.github.rahul_gill.attendance.notification
import android.app.Notification
import android.graphics.drawable.Icon
import android.content.Context
fun test(context: Context) {
    val style = Notification.ProgressStyle()
    style.setProgressTrackerIcon(Icon.createWithResource(context, 123))
    val builder = Notification.Builder(context, "id")
        .setStyle(style)
}
