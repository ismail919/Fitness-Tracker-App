package com.example.fitnessapp

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoresTest {

    @Test
    fun strain_halfOfGoal_is50() {
        assertEquals(50, Scores.strain(4000, 8000))
    }

    @Test
    fun strain_overGoal_isCappedAt100() {
        assertEquals(100, Scores.strain(20000, 8000))
    }

    @Test
    fun strain_zeroGoal_isZero() {
        assertEquals(0, Scores.strain(1000, 0))
    }

    @Test
    fun recovery_fullSleepAndFeelingGreat_is100() {
        assertEquals(100, Scores.recovery(8.0, 5))
    }

    @Test
    fun recovery_halfSleepAndOkayFeeling_is50() {
        assertEquals(50, Scores.recovery(4.0, 3))
    }

    @Test
    fun recovery_noSleepAndFeelingTerrible_is0() {
        assertEquals(0, Scores.recovery(0.0, 1))
    }

    @Test
    fun recovery_outOfRangeInputs_areClamped() {
        assertEquals(100, Scores.recovery(12.0, 9))
    }
}