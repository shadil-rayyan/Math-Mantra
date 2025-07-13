import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import com.zendalona.zmantra.view.SettingFragment
import com.zendalona.zmantra.R
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    private lateinit var scenario: FragmentScenario<SettingFragment>

    @Before
    fun setUp() {
        // Launch the SettingFragment directly without using @Rule
        scenario = FragmentScenario.launchInContainer(SettingFragment::class.java)
    }

    // Test Language Spinner
    @Test
    fun testLanguageSpinner() {
        // Open the fragment containing the spinner
        onView(withId(R.id.language_spinner)).perform(click())

        // Check for a valid language (e.g., "English")
        onView(withText("English")).perform(click())

        // Check if the selected language is properly displayed in the spinner
        onView(withId(R.id.language_spinner)).check(matches(withSpinnerText("English")))

        // Optionally check for an invalid selection if needed
        // onView(withText("Invalid Language")).check(doesNotExist())  // Example of invalid data handling
    }


    // Test Contrast Radio Buttons
    @Test
    fun testContrastRadioButtons() {
        // Check the initial default contrast mode (should be "Follow System")
        onView(withId(R.id.contrast_default)).check(matches(isChecked()))

        // Select "White on Black" mode
        onView(withId(R.id.contrast_white_on_black)).perform(click())
        onView(withId(R.id.contrast_white_on_black)).check(matches(isChecked()))

        // Select "Black on White" mode
        onView(withId(R.id.contrast_black_on_white)).perform(click())
        onView(withId(R.id.contrast_black_on_white)).check(matches(isChecked()))
    }

    // Test Difficulty Radio Buttons
    @Test
    fun testDifficultyRadioButtons() {
        // Check the default difficulty (Simple)
        onView(withId(R.id.difficulty_simple)).check(matches(isChecked()))

        // Select "Medium" difficulty
        onView(withId(R.id.difficulty_medium)).perform(click())
        onView(withId(R.id.difficulty_medium)).check(matches(isChecked()))

        // Select "Hard" difficulty
        onView(withId(R.id.difficulty_hard)).perform(click())
        onView(withId(R.id.difficulty_hard)).check(matches(isChecked()))
    }

    // Test Speech Rate Controls
    @Test
    fun testSpeechRateControls() {
        // Check initial speech rate (should be 24)
        onView(withId(R.id.speech_rate_value)).check(matches(withText("24")))

        // Increase speech rate
        onView(withId(R.id.speech_rate_increase)).perform(click())
        onView(withId(R.id.speech_rate_value)).check(matches(withText("25")))

        // Decrease speech rate
        onView(withId(R.id.speech_rate_decrease)).perform(click())
        onView(withId(R.id.speech_rate_value)).check(matches(withText("24")))
    }

    // Test Background Music Toggle
    @Test
    fun testBackgroundMusicToggle() {
        // Check initial state of the music toggle (should be off)
        onView(withId(R.id.background_music_toggle)).check(matches(not(isChecked())))

        // Turn on the music
        onView(withId(R.id.background_music_toggle)).perform(click())
        onView(withId(R.id.background_music_toggle)).check(matches(isChecked()))

        // Turn off the music
        onView(withId(R.id.background_music_toggle)).perform(click())
        onView(withId(R.id.background_music_toggle)).check(matches(not(isChecked())))
    }

    // Test Music Volume Controls
    @Test
    fun testMusicVolumeControls() {
        // Check initial volume (should be 24)
        onView(withId(R.id.music_volume_value)).check(matches(withText("24")))

        // Decrease volume
        onView(withId(R.id.music_volume_decrease)).perform(click())
        onView(withId(R.id.music_volume_value)).check(matches(withText("23")))

        // Increase volume
        onView(withId(R.id.music_volume_increase)).perform(click())
        onView(withId(R.id.music_volume_value)).check(matches(withText("24")))
    }

    // Test Reset Settings Button
    @Test
    fun testResetButton() {
        // Change some settings (e.g., difficulty, language, etc.)
        onView(withId(R.id.difficulty_medium)).perform(click())
        onView(withId(R.id.language_spinner)).perform(click())
        onView(withText("English")).perform(click())

        // Click reset button
        onView(withId(R.id.reset_settings_button)).perform(click())

        // Check if settings are reset to default (e.g., difficulty should be Simple)
        onView(withId(R.id.difficulty_simple)).check(matches(isChecked()))
        onView(withId(R.id.language_spinner)).check(matches(withSpinnerText("default")))
    }
}
