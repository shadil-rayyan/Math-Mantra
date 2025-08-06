package com.zendalona.zmantra.domain.repository

import com.zendalona.zmantra.domain.model.GameQuestion

interface QuestionRepository {
    suspend fun getQuestions(mode: String, difficulty: String): List<GameQuestion>
}
