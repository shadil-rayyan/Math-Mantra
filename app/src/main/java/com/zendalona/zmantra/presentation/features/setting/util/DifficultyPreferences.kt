package com.zendalona.zmantra.presentation.features.setting.util

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

object DifficultyPreferences {
    private const val KEY_DIFFICULTY = "pref_difficulty_level"

    fun setDifficulty(context: Context, level: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit() { putInt(KEY_DIFFICULTY, level) }
    }

    // This function accepts a nullable Context and handles the null case
    fun getDifficulty(context: Context?): Int {
        if (context == null) {
            return 1 // Return a default value if the context is null
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(KEY_DIFFICULTY, 1)
    }

}

