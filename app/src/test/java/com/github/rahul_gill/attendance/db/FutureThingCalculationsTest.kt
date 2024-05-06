package com.github.rahul_gill.attendance.db

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue

@RunWith(JUnit4::class)
class FutureThingCalculationsTest {

    @Test
    fun `test some cases and check if future things calculations are correct`() {
        var presents = 6
        var absents = 0
        var requiredPercentage = 75
        assertTrue(
            FutureThingCalculations.neededPresentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 0
        )
        assertTrue(
            FutureThingCalculations.allowedAbsentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 2
        )
        presents = 6
        absents = 1
        requiredPercentage = 75
        assertTrue(
            FutureThingCalculations.neededPresentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 0
        )
        assertTrue(
            FutureThingCalculations.allowedAbsentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 1
        )
        presents = 6
        absents = 2
        requiredPercentage = 75
        assertTrue(
            FutureThingCalculations.neededPresentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 0
        )
        assertTrue(
            FutureThingCalculations.allowedAbsentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 0
        )
        presents = 5
        absents = 2
        requiredPercentage = 75
        assertTrue(
            FutureThingCalculations.neededPresentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 1
        )
        assertTrue(
            FutureThingCalculations.allowedAbsentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 0
        )

        presents = 4
        absents = 2
        requiredPercentage = 75
        assertTrue(
            FutureThingCalculations.neededPresentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 2
        )
        assertTrue(
            FutureThingCalculations.allowedAbsentsInFuture(
                presents,
                absents,
                requiredPercentage
            ) == 0
        )
    }
}