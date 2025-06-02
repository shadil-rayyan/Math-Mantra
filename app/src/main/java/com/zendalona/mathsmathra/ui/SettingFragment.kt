package com.zendalona.mathsmathra.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.zendalona.mathsmathra.R
import com.zendalona.mathsmathra.databinding.FragmentSettingsBinding
import com.zendalona.mathsmathra.utility.settings.LocaleHelper
import java.util.Locale

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial selection for language
        val currentLang = LocaleHelper.getLanguage(requireContext())
        when (currentLang) {
            "en" -> binding.radioLanguageEn.isChecked = true
            "ml" -> binding.radioLanguageMl.isChecked = true
            else -> binding.radioLanguageDefault.isChecked = true
        }

        // Set initial selection for contrast
        val currentContrast = prefs.getInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_NO)
        when (currentContrast) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioContrastLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioContrastDark.isChecked = true
            else -> binding.radioContrastDefault.isChecked = true
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val langCode = when (checkedId) {
                R.id.radio_language_en -> "en"
                R.id.radio_language_ml -> "ml"
                else -> Locale.getDefault().language
            }
            val currentLang = LocaleHelper.getLanguage(requireContext())
            if (currentLang != langCode) {
                LocaleHelper.setLocale(requireContext(), langCode)
                prefs.edit().putString("Locale.Helper.Selected.Language", langCode).apply()
                requireActivity().recreate()  // only recreate if changed
            }
        }


        binding.contrastRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radio_contrast_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radio_contrast_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            prefs.edit().putInt("app_contrast_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            requireActivity().recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
