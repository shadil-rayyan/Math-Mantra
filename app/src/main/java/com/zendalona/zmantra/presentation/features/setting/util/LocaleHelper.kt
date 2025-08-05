package com.zendalona.zmantra.presentation.features.setting.util

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    @JvmStatic
    fun getLanguage(context: Context?): String {
        val prefs = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val savedLang = prefs?.getString(SELECTED_LANGUAGE, null)

        // Return saved language, otherwise get system language, or default to "en"
        return savedLang ?: getSystemLanguage(context)
    }

    private fun getSystemLanguage(context: Context?): String {
        return if (context != null) {
            context.resources.configuration.locales[0].language
        } else {
            // Fallback language if context is null
            "en"
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
        prefs.edit { putString(SELECTED_LANGUAGE, language) }
    }

    private fun clearLanguage(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit { remove(SELECTED_LANGUAGE) }
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}