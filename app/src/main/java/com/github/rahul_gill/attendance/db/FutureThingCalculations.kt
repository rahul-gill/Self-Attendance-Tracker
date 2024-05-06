package com.github.rahul_gill.attendance.db

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.rahul_gill.attendance.R
import kotlin.math.max
import kotlin.math.min


object FutureThingCalculations {
    fun allowedAbsentsInFuture(presents: Int, absents: Int, requiredPercentage: Int): Int {
        val exp =
            ((100 - requiredPercentage) * presents - requiredPercentage * absents) / requiredPercentage
        return max(0, exp)
    }

    fun neededPresentsInFuture(presents: Int, absents: Int, requiredPercentage: Int): Int {
        val exp =
            (requiredPercentage * absents - (100 - requiredPercentage) * presents) / (100 - requiredPercentage)
        return max(0, exp)
    }

    @Composable
    fun getMessageForFuture(presents: Int, absents: Int, requiredPercentage: Int): String {
        val presentsNeeded = neededPresentsInFuture(presents, absents, requiredPercentage)
        val absentsAllowed = allowedAbsentsInFuture(presents, absents, requiredPercentage)
        return when {
            absentsAllowed != 0 -> stringResource(
                id = R.string.can_be_absent_in_n_classes,
                absentsAllowed
            )
            presentsNeeded != 0 -> stringResource(
                id = R.string.need_to_attend_n_more_classes,
                presentsNeeded
            )
            else -> stringResource(id = R.string.cannot_miss_next_class)
        }
    }
}
