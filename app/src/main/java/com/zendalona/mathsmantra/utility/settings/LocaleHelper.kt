package com.zendalona.mathsmantra.utility.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    @JvmStatic
    fun getLanguage(context: Context?): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(SELECTED_LANGUAGE, getSystemLanguage(context!!)) ?: getSystemLanguage(context)
    }

    private fun getSystemLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language
        }
    }

    @JvmStatic
    fun setLocale(context: Context, language: String?): Context {
        if (language == null || language == "default") {
            clearLanguage(context)
            return updateResources(context, Locale.getDefault().language)
        }
        persistLanguage(context, language)
        return updateResources(context, language)
    }

    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return updateResources(context, lang)
    }

    private fun persistLanguage(context: Context, language: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    private fun clearLanguage(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().remove(SELECTED_LANGUAGE).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }
}
