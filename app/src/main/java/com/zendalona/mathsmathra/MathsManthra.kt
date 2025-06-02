package com.zendalona.mathsmathra

import android.app.Application
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.zendalona.mathsmathra.utility.settings.LocaleHelper
class MathsManthra : Application() {
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
