package com.zendalona.mathsmantra

import android.app.Application
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
class MathsMantra : Application() {
    override fun onCreate() {
        super.onCreate()

        // Apply saved locale before anything else
        LocaleHelper.onAttach(this)

        // Load contrast mode from preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val contrast = prefs.getInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(contrast)
    }
}
