package com.github.rahul_gill.attendance.db

import app.cash.sqldelight.ColumnAdapter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object LocalDateAdapter : ColumnAdapter<LocalDate, String> {
    override fun decode(databaseValue: String): LocalDate = LocalDate.parse(databaseValue)
    override fun encode(value: LocalDate): String = value.format(DateTimeFormatter.ISO_DATE)
}

object LocalTimeAdapter : ColumnAdapter<LocalTime, String> {
    override fun decode(databaseValue: String): LocalTime = LocalTime.parse(databaseValue)
    override fun encode(value: LocalTime): String = value.format(DateTimeFormatter.ISO_TIME)
}

object DayOfWeekAdapter : ColumnAdapter<DayOfWeek, Long> {
    override fun decode(databaseValue: Long): DayOfWeek {
        return if(databaseValue == 0L) DayOfWeek.SUNDAY
        else DayOfWeek.of(databaseValue.toInt())
    }
    override fun encode(value: DayOfWeek): Long {
        return (value.value % 7).toLong()
    }
}