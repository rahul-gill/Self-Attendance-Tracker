package com.github.rahul_gill.attendance.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy")

fun formatWeek(startDate: LocalDate): String {
    val endDate = startDate.plusWeeks(1)
    return when{
        startDate.year == endDate.year && startDate.month == endDate.month ->
            startDate.dayOfMonth.toString() + " - " + endDate.format(DateTimeFormatter.ofPattern("d MMM, yyyy"))
        startDate.year == endDate.year ->
            startDate.format(DateTimeFormatter.ofPattern("d MMM - ")) +endDate.format(dateFormatter)
        else ->
            startDate.format(dateFormatter) + " - " + endDate.format(dateFormatter)
    }
}