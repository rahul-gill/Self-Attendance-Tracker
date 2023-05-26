package com.github.rahul_gill.attendance.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy")