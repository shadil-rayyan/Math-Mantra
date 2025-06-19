package com.zendalona.mathsmantra

import android.app.Application
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
class MathsMantra : Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")


        // Apply saved locale before anything else
        LocaleHelper.onAttach(this)

        // Load contrast mode from preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val contrast = prefs.getInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(contrast)
    }
}
