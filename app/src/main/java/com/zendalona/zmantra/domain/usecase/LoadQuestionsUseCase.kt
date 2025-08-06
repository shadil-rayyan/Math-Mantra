package com.zendalona.zmantra.domain.usecase

import com.zendalona.zmantra.domain.model.GameQuestion
import com.zendalona.zmantra.domain.repository.QuestionRepository
import javax.inject.Inject
class LoadQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(mode: String, difficulty: String): List<GameQuestion> {
        return repository.getQuestions(mode, difficulty)
    }
}
