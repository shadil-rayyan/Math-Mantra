package com.zendalona.mathsmanthra.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.zendalona.mathsmanthra.Enum.Difficulty
import com.zendalona.mathsmanthra.utility.settings.BackgroundMusicPlayer
import com.zendalona.mathsmanthra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmanthra.utility.settings.LocaleHelper
import com.zendalona.mathsmanthra.utility.settings.TTSUtility
import com.zendalona.mathsmanthra.R
import com.zendalona.mathsmanthra.databinding.FragmentSettingsBinding

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var ttsUtility: TTSUtility
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsEditor: SharedPreferences.Editor

    private val languageCodeMap = mapOf(
        0 to "default",
        1 to "en",
        2 to "ml"
    )

    private var languageSpinnerInitialized = false  // prevent initial callback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefsEditor = prefs.edit()

        BackgroundMusicPlayer.initialize(requireContext())
        ttsUtility = TTSUtility(requireContext())

        setupLanguageSpinner()
        setupContrastRadioButtons()
        setupDifficultyRadioButtons()
        setupSpeechRateControls()
        setupMusicToggle()
        setupMusicVolumeControls()
        setupResetButton()
    }

    private fun setupLanguageSpinner() {
        val currentLang = LocaleHelper.getLanguage(requireContext())
        val selectedIndex = languageCodeMap.entries.find { it.value == currentLang }?.key ?: 0

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter
        }

        binding.languageSpinner.setSelection(selectedIndex)

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!languageSpinnerInitialized) {
                    languageSpinnerInitialized = true
                    return
                }

                val selectedLangCode = languageCodeMap[position]
                val currentLang = LocaleHelper.getLanguage(requireContext())

                if (selectedLangCode == "default") {
                    LocaleHelper.setLocale(requireContext(), null)
                    prefsEditor.remove("Locale.Helper.Selected.Language").apply()
                    requireActivity().recreate()
                } else if (selectedLangCode != null && selectedLangCode != currentLang) {
                    LocaleHelper.setLocale(requireContext(), selectedLangCode)
                    prefsEditor.putString("Locale.Helper.Selected.Language", selectedLangCode).apply()
                    requireActivity().recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupContrastRadioButtons() {
        val currentContrast = prefs.getInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_NO)

        when (currentContrast) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.contrastDefault.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.contrastWhiteOnBlack.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.contrastBlackOnWhite.isChecked = true
        }

        binding.contrastRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.contrast_black_on_white -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.contrast_white_on_black -> AppCompatDelegate.MODE_NIGHT_YES
                R.id.contrast_default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            prefsEditor.putInt("app_contrast_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            requireActivity().recreate()
        }
    }

    private fun setupDifficultyRadioButtons() {
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())
        when (difficulty) {
            Difficulty.EASY -> binding.difficultyEasy.isChecked = true
            Difficulty.MEDIUM -> binding.difficultyMedium.isChecked = true
            Difficulty.HARD -> binding.difficultyHard.isChecked = true
        }

        binding.difficultyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedDifficulty = when (checkedId) {
                R.id.difficulty_easy -> Difficulty.EASY
                R.id.difficulty_medium -> Difficulty.MEDIUM
                R.id.difficulty_hard -> Difficulty.HARD
                else -> Difficulty.EASY
            }
            DifficultyPreferences.setDifficulty(requireContext(), selectedDifficulty)
        }
    }

    private fun setupSpeechRateControls() {
        var speechRate = prefs.getFloat("tts_speed", 1.0f)
        binding.speechRateValue.text = String.format("%.1f", speechRate)
        ttsUtility.setSpeechRate(speechRate)

        binding.speechRateIncrease.setOnClickListener {
            if (speechRate < 3.0f) {
                speechRate += 0.1f
                speechRate = (speechRate * 10).toInt() / 10f
                prefsEditor.putFloat("tts_speed", speechRate).apply()
                binding.speechRateValue.text = String.format("%.1f", speechRate)
                ttsUtility.setSpeechRate(speechRate)
                Log.d("SettingFragment", "Speech rate increased to $speechRate")
            }
        }

        binding.speechRateDecrease.setOnClickListener {
            if (speechRate > 0.5f) {
                speechRate -= 0.1f
                speechRate = (speechRate * 10).toInt() / 10f
                prefsEditor.putFloat("tts_speed", speechRate).apply()
                binding.speechRateValue.text = String.format("%.1f", speechRate)
                ttsUtility.setSpeechRate(speechRate)
                Log.d("SettingFragment", "Speech rate decreased to $speechRate")
            }
        }
    }

    private fun setupMusicToggle() {
        val musicSwitch = binding.backgroundMusicToggle
        musicSwitch.isChecked = prefs.getBoolean("music_enabled", false)
        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsEditor.putBoolean("music_enabled", isChecked).apply()
            Log.d("SettingFragment", "Music toggle changed to $isChecked")

            if (isChecked) {
                BackgroundMusicPlayer.startMusic()
            } else {
                BackgroundMusicPlayer.pauseMusic()
            }
        }
    }

    private fun setupMusicVolumeControls() {
        var currentVolume = BackgroundMusicPlayer.getVolume()
        binding.musicVolumeValue.text = String.format("%.1f", currentVolume)

        binding.musicVolumeDecrease.setOnClickListener {
            currentVolume = (currentVolume - 0.1f).coerceAtLeast(0.1f)
            BackgroundMusicPlayer.setVolume(currentVolume)
            binding.musicVolumeValue.text = String.format("%.1f", currentVolume)
        }

        binding.musicVolumeIncrease.setOnClickListener {
            currentVolume = (currentVolume + 0.1f).coerceAtMost(1.0f)
            BackgroundMusicPlayer.setVolume(currentVolume)
            binding.musicVolumeValue.text = String.format("%.1f", currentVolume)
        }
    }

    private fun setupResetButton() {
        binding.resetSettingsButton.setOnClickListener {
            prefsEditor.putBoolean("music_enabled", false)
            prefsEditor.putFloat("tts_speed", 1.0f)
            prefsEditor.putInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_NO)
            prefsEditor.remove("Locale.Helper.Selected.Language")
            DifficultyPreferences.setDifficulty(requireContext(), Difficulty.EASY)
            prefsEditor.apply()

            binding.backgroundMusicToggle.isChecked = false
            binding.speechRateValue.text = "1.0"
            ttsUtility.setSpeechRate(1.0f)

            binding.contrastBlackOnWhite.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            binding.languageSpinner.setSelection(0)
            languageSpinnerInitialized = false

            binding.difficultyEasy.isChecked = true

            BackgroundMusicPlayer.pauseMusic()

            Log.d("SettingFragment", "Settings reset to defaults")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ttsUtility.shutdown()
    }
}
