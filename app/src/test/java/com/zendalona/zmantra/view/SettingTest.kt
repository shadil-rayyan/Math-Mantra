package com.zendalona.zmantra.view

import android.content.SharedPreferences
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import com.zendalona.zmantra.core.utility.settings.BackgroundMusicPlayer
import com.zendalona.zmantra.core.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.core.utility.settings.LocaleHelper
import com.zendalona.zmantra.core.utility.common.TTSUtility
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.Enum.Difficulty
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument.Config
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify

@Config(manifest=Config.NONE) // Robolectric does not need the full Android Manifest
class SettingFragmentTest {

    private lateinit var settingFragment: SettingFragment
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockTTSUtility: TTSUtility
    private lateinit var mockMusicPlayer: BackgroundMusicPlayer
    private lateinit var mockLocaleHelper: LocaleHelper
    private lateinit var mockDifficultyPreferences: DifficultyPreferences

    @Before
    fun setup() {
        // Create mock dependencies
        mockPrefs = Mockito.mock(SharedPreferences::class.java)
        mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        mockTTSUtility = Mockito.mock(TTSUtility::class.java)
        mockMusicPlayer = Mockito.mock(BackgroundMusicPlayer::class.java)
        mockLocaleHelper = Mockito.mock(LocaleHelper::class.java)
        mockDifficultyPreferences = Mockito.mock(DifficultyPreferences::class.java)

        // Simulate application context for the fragment
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Set up the Fragment and bind mock dependencies
        settingFragment = SettingFragment().apply {
            // Attach mock dependencies to the fragment's methods or fields.
            prefs = mockPrefs
            prefsEditor = mockEditor
            ttsUtility = mockTTSUtility
        }

        // Create Robolectric controller for fragment
        Robolectric.buildFragment(SettingFragment::class.java).apply {
            settingFragment = get()
            settingFragment.onViewCreated(settingFragment.view!!, null)
        }
    }

    @Test
    fun `test setup language spinner should update language in preferences`() {
        // Set mock behavior
        val mockSpinner = Mockito.mock(Spinner::class.java)
        settingFragment.binding.languageSpinner = mockSpinner

        // Simulate language change
        settingFragment.binding.languageSpinner.setSelection(1) // Assume "English" is selected

        // Verify that setLocale and preferences are updated correctly
        verify(mockLocaleHelper).setLocale(any(), "en")
        verify(mockEditor).putString("Locale.Helper.Selected.Language", "en")
    }

    @Test
    fun `test setup contrast radio buttons should save contrast mode to preferences`() {
        // Assume contrast mode is set to white on black
        settingFragment.binding.contrastWhiteOnBlack.isChecked = true

        // Simulate radio button change listener triggered
        settingFragment.binding.contrastRadioGroup.check(R.id.contrast_white_on_black)

        // Verify that the contrast mode is saved in preferences
        verify(mockEditor).putInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_YES)
        verify(mockEditor).apply()
        verify(mockLocaleHelper).setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    @Test
    fun `test difficulty radio buttons should update difficulty preference`() {
        // Assume medium difficulty is selected
        settingFragment.binding.difficultyMedium.isChecked = true

        // Simulate radio button change listener triggered
        settingFragment.binding.difficultyRadioGroup.check(R.id.difficulty_medium)

        // Verify that the difficulty preference is updated correctly
        verify(mockDifficultyPreferences).setDifficulty(any(), Difficulty.MEDIUM)
    }

    @Test
    fun `test speech rate increase should update preferences and TTS`() {
        // Initial speech rate
        val initialRate = 1.0f
        whenever(mockPrefs.getFloat("tts_speed", 1.0f)).thenReturn(initialRate)

        // Simulate increase in speech rate
        settingFragment.binding.speechRateIncrease.performClick()

        // Verify that preferences and TTS utility are updated with new speech rate
        verify(mockEditor).putFloat("tts_speed", 1.1f)
        verify(mockTTSUtility).setSpeechRate(1.1f)
    }

    @Test
    fun `test background music toggle should update preferences and start music`() {
        // Assume music is off initially
        whenever(mockPrefs.getBoolean("music_enabled", false)).thenReturn(false)

        // Simulate music toggle change
        settingFragment.binding.backgroundMusicToggle.isChecked = true
        settingFragment.binding.backgroundMusicToggle.performClick()

        // Verify that music preferences are updated and music is started
        verify(mockEditor).putBoolean("music_enabled", true)
        verify(mockMusicPlayer).startMusic()
    }

    @Test
    fun `test reset button should reset all settings to defaults`() {
        // Simulate pressing the reset button
        settingFragment.binding.resetSettingsButton.performClick()

        // Verify that preferences are reset to default values
        verify(mockEditor).putBoolean("music_enabled", false)
        verify(mockEditor).putFloat("tts_speed", 1.0f)
        verify(mockEditor).putInt("app_contrast_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        verify(mockEditor).remove("Locale.Helper.Selected.Language")
        verify(mockEditor).apply()

        // Verify other reset actions (e.g., music stop)
        verify(mockMusicPlayer).pauseMusic()
    }
}
