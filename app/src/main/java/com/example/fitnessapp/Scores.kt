package com.example.fitnessapp

import kotlin.math.roundToInt

object Scores {

    // 0-100: how much of your daily step goal you have used
    fun strain(steps: Int, goal: Int): Int {
        if (goal <= 0) return 0
        return ((steps.toDouble() / goal) * 100).coerceIn(0.0, 100.0).roundToInt()
    }

    // 0-100: sleep counts for 70, how you feel (1-5) counts for 30
    fun recovery(sleepHours: Double, feeling: Int): Int {
        val sleepPart = (sleepHours / 8.0).coerceIn(0.0, 1.0) * 70
        val feelingPart = ((feeling.coerceIn(1, 5) - 1) / 4.0) * 30
        return (sleepPart + feelingPart).roundToInt()
    }
}