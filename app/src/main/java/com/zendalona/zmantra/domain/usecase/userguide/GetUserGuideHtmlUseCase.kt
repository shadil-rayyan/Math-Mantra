package com.zendalona.zmantra.domain.usecase.userguide;

import com.zendalona.zmantra.domain.repository.UserGuideRepository
import javax.inject.Inject

class GetUserGuideHtmlUseCase @Inject constructor(
        private val repository: UserGuideRepository
) {
    suspend operator fun invoke(language: String): String {
        return repository.getUserGuideHtml(language)
    }
}

