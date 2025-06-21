package com.zendalona.mathsmantra.utility.settings

import android.content.Context
import android.preference.PreferenceManager

object DifficultyPreferences {
    private const val KEY_DIFFICULTY = "pref_difficulty_level"

    fun setDifficulty(context: Context, level: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(KEY_DIFFICULTY, level).apply()
    }

    fun getDifficulty(context: Context?): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(KEY_DIFFICULTY, 1) // default is SIMPLE = 1
    }
}
