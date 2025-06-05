package com.zendalona.mathsmantra.utility.settings

import android.content.Context
import android.preference.PreferenceManager

object DifficultyPreferences {
    private const val KEY_DIFFICULTY = "pref_difficulty"

    @JvmStatic
    fun setDifficulty(context: Context, difficulty: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(KEY_DIFFICULTY, difficulty).apply()
    }

    @JvmStatic
    fun getDifficulty(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(KEY_DIFFICULTY, "medium") ?: "medium"
    }
}
