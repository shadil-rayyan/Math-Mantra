package com.zendalona.zmantra

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.zendalona.zmantra.presentation.features.setting.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
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
