package com.zendalona.zmantra.data.repository.userguide;

import android.content.Context
import com.zendalona.zmantra.domain.repository.UserGuideRepository
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject

class UserGuideRepositoryImpl @Inject constructor(
        @ApplicationContext private val context: Context
) : UserGuideRepository {

    override suspend fun getUserGuideHtml(language: String): String {
        return try {
            context.assets.open("userguide/$language.html").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "<p>Could not load user guide.</p>"
        }
    }
}

