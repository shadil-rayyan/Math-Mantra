package com.zendalona.zmantra.domain.usecase.shake

import javax.inject.Inject

class ValidateShakeAnswerUseCase @Inject constructor() {
    fun execute(
        userAnswer: Int,
        correctAnswer: Int,
        wrongAttempts: Int,
        maxAttempts: Int = 3
    ): ValidationResult {
        return when {
            userAnswer == correctAnswer -> ValidationResult.Correct
            wrongAttempts + 1 >= maxAttempts -> ValidationResult.ShowCorrect
            else -> ValidationResult.Wrong
        }
    }
}

sealed class ValidationResult {
    object Correct : ValidationResult()
    object Wrong : ValidationResult()
    object ShowCorrect : ValidationResult()
}
