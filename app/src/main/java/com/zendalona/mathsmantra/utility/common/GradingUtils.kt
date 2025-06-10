package com.zendalona.mathsmantra.utility.common

object GradingUtils {

    private val gradePoints = mapOf(
        "Excellent" to 50,
        "Very Good" to 40,
        "Good" to 30,
        "Not Bad" to 20,
        "Okay" to 10,
        "Wrong Answer" to -10
    )

    fun getGrade(elapsedTime: Double, totalTime: Double, isCorrect: Boolean): String {
        if (!isCorrect) return "Wrong Answer"

        return when {
            elapsedTime < totalTime * 0.5 -> "Excellent"
            elapsedTime < totalTime * 0.75 -> "Very Good"
            elapsedTime < totalTime -> "Good"
            elapsedTime < totalTime * 1.25 -> "Not Bad" // renamed from Fair
            else -> "Okay"
        }
    }

    fun getPointsForGrade(grade: String): Int {
        return gradePoints[grade] ?: 0
    }
}
