package com.github.rahul_gill.attendance.db

import com.github.rahul_gill.attendance.R

enum class CourseClassStatus {
    Present,
    Absent,
    Cancelled,
    Unset;

    override fun toString() = when (this) {
        Present -> "Present"
        Absent -> "Absent"
        Cancelled -> "Cancelled"
        Unset -> "Unset"

    }

    companion object {
        fun fromString(str: String) = when (str) {
            "Present" -> Present
            "Absent" -> Absent
            "Cancelled" -> Cancelled
            "Unset" -> Unset
            else -> throw IllegalArgumentException("Status can only be either one of these: Present, Absent, Cancelled, Unset")
        }
    }
}