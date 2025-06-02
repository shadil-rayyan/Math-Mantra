package com.zendalona.mathsmathra.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    // Map spinner position to language codes
    private val languageCodeMap = mapOf(
        0 to "default",  // Assuming first item is default language
        1 to "en",
        2 to "ml"
        // Add more if you have more languages in your array
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageSpinner()
        setupContrastRadioButtons()
        setupSpeechVolumeControls()
        setupSpeechRateControls()
    }

    private fun setupLanguageSpinner() {
        // Set initial selection based on current language
        val currentLang = LocaleHelper.getLanguage(requireContext())

        // Find spinner index for currentLang or fallback to 0 (default)
        val selectedIndex = languageCodeMap.entries.find { it.value == currentLang }?.key ?: 0

        // Setup ArrayAdapter with the language_levels array resource
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter
        }

        binding.languageSpinner.setSelection(selectedIndex)

        // Spinner listener
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLangCode = languageCodeMap[position] ?: Locale.getDefault().language

                val currentLang = LocaleHelper.getLanguage(requireContext())

                if (selectedLangCode != currentLang && selectedLangCode != "default") {
                    LocaleHelper.setLocale(requireContext(), selectedLangCode)
                    prefs.edit().putString("Locale.Helper.Selected.Language", selectedLangCode).apply()
                    requireActivity().recreate()
                } else if (selectedLangCode == "default") {
                    // If "default" selected, reset to system language
                    LocaleHelper.setLocale(requireContext(), Locale.getDefault().language)
                    prefs.edit().remove("Locale.Helper.Selected.Language").apply()
                    requireActivity().recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupContrastRadioButtons() {
        val currentContrast = prefs.getInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_NO)

        // Set initial checked button based on stored preference
        when (currentContrast) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.contrastDefault.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.contrastWhiteOnBlack.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.contrastBlackOnWhite.isChecked = true
            else -> binding.contrastDefault.isChecked = true
        }

        binding.contrastRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.contrast_black_on_white -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.contrast_white_on_black -> AppCompatDelegate.MODE_NIGHT_YES
                R.id.contrast_default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            prefs.edit().putInt("app_contrast_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            requireActivity().recreate()
        }
    }

    private fun setupSpeechVolumeControls() {
        val currentVolume = prefs.getFloat("tts_volume", 1.0f)
        val currentSpeed = prefs.getFloat("tts_speed", 1.0f)

        // Set UI text values
        binding.speechVolumeValue.text = String.format("%.1f", currentVolume)

        binding.speechVolumeDecrease.setOnClickListener {
            val newVolume = (prefs.getFloat("tts_volume", 1.0f) - 0.1f).coerceAtLeast(0.1f)
            prefs.edit().putFloat("tts_volume", newVolume).apply()
            binding.speechVolumeValue.text = String.format("%.1f", newVolume)
        }

        binding.speechVolumeIncrease.setOnClickListener {
            val newVolume = (prefs.getFloat("tts_volume", 1.0f) + 0.1f).coerceAtMost(1.0f)
            prefs.edit().putFloat("tts_volume", newVolume).apply()
            binding.speechVolumeValue.text = String.format("%.1f", newVolume)
        }
    }

    private fun setupSpeechRateControls() {
        val currentSpeed = prefs.getFloat("tts_speed", 1.0f)

        // Initialize UI text with current speed
        binding.speechRateValue.text = String.format("%.1f", currentSpeed)

        binding.speechRateDecrease.setOnClickListener {
            val newSpeed = (prefs.getFloat("tts_speed", 1.0f) - 0.1f).coerceAtLeast(0.1f)
            prefs.edit().putFloat("tts_speed", newSpeed).apply()
            binding.speechRateValue.text = String.format("%.1f", newSpeed)
        }

        binding.speechRateIncrease.setOnClickListener {
            val newSpeed = (prefs.getFloat("tts_speed", 1.0f) + 0.1f).coerceAtMost(2.0f)  // 2.0f max speed
            prefs.edit().putFloat("tts_speed", newSpeed).apply()
            binding.speechRateValue.text = String.format("%.1f", newSpeed)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
