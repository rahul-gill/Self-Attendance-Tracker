package com.github.rahul_gill.attendance.util

import android.content.Context
import android.util.Log
import androidx.startup.Initializer


private var appContext: Context? = null

val applicationContextGlobal
    get() = appContext!!


internal class ApplicationContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        context.applicationContext.also { appContext = it }
        Log.i("ApplicationContextInitializer", "init done")
        return context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

