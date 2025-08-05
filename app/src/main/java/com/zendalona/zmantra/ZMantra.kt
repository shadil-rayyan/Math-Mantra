package com.zendalona.zmantra

import android.app.Application
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.zendalona.zmantra.core.utility.settings.LocaleHelper
class ZMantra : Application() {
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
