package com.zendalona.zmantra.domain.repository;

interface UserGuideRepository {
    suspend fun getUserGuideHtml(language: String): String
}


