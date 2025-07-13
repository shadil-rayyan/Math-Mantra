import android.content.Context
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.zmantra.view.LandingPageFragment
import com.zendalona.zmantra.utility.settings.BackgroundMusicPlayer
import com.zendalona.zmantra.utility.common.TTSUtility
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.junit.Assert.*
import android.content.SharedPreferences
import com.zendalona.zmantra.databinding.FragmentLandingPageBinding
import com.zendalona.zmantra.view.FragmentNavigation

class LandingPageFragmentTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockNavigationListener: FragmentNavigation

    @Mock
    private lateinit var mockTTSUtility: TTSUtility

    @Mock
    private lateinit var mockBackgroundMusicPlayer: BackgroundMusicPlayer

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockBinding: FragmentLandingPageBinding

    private lateinit var landingPageFragment: LandingPageFragment

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)  // For older Mockito versions (prior to 3.x)
        landingPageFragment = LandingPageFragment()

        // Mock the context and preferences
        Mockito.`when`(mockContext.getSharedPreferences("default", Context.MODE_PRIVATE)).thenReturn(mockPrefs)

        // Inject dependencies
        landingPageFragment = LandingPageFragment()

        // Simulate FragmentNavigation listener attachment
        landingPageFragment.onAttach(mockContext)
        landingPageFragment.navigationListener = mockNavigationListener

        // Mock the preferences for TTS and music settings
        Mockito.`when`(mockPrefs.getFloat("tts_speed", 1.0f)).thenReturn(1.2f)
        Mockito.`when`(mockPrefs.getBoolean("music_enabled", false)).thenReturn(true)
    }

    @Test
    fun testOnCreateView_SetupCorrectly() {
        // Verify that the `navigationListener` is correctly set when the fragment is attached.
        assertNotNull(landingPageFragment.navigationListener)

        // Test that the TTS utility is set up with correct speech rate.
        landingPageFragment.onViewCreated(mockBinding.root, null)
        Mockito.verify(mockTTSUtility).setSpeechRate(1.2f)

        // Verify that the background music is started if `music_enabled` is true.
        landingPageFragment.onViewCreated(mockBinding.root, null)
        Mockito.verify(mockBackgroundMusicPlayer).startMusic()
    }

    @Test
    fun testSettingsButtonClick() {
        // Simulate Settings button click
        landingPageFragment.binding.settings.performClick()

        // Verify that the correct fragment is loaded
        Mockito.verify(mockNavigationListener).loadFragment(Mockito.any(), Mockito.eq(FragmentTransaction.TRANSIT_FRAGMENT_OPEN))
    }

    @Test
    fun testQuickplayButtonClick() {
        // Simulate Quickplay button click
        landingPageFragment.binding.quickplay.performClick()

        // Verify that the QuickPlayFragment is loaded
        Mockito.verify(mockNavigationListener).loadFragment(Mockito.any(), Mockito.eq(FragmentTransaction.TRANSIT_FRAGMENT_OPEN))
    }

    @Test
    fun testLearningButtonClick() {
        // Simulate Learning button click
        landingPageFragment.binding.learningButton.performClick()

        // Verify that the LearningFragment is loaded
        Mockito.verify(mockNavigationListener).loadFragment(Mockito.any(), Mockito.eq(FragmentTransaction.TRANSIT_FRAGMENT_OPEN))
    }

    @Test
    fun testGameButtonClick() {
        // Simulate Game button click
        landingPageFragment.binding.GameButton.performClick()

        // Verify that the GameFragment is loaded
        Mockito.verify(mockNavigationListener).loadFragment(Mockito.any(), Mockito.eq(FragmentTransaction.TRANSIT_FRAGMENT_OPEN))
    }

    @Test
    fun testUserGuideButtonClick() {
        // Simulate User Guide button click
        landingPageFragment.binding.userGuide.performClick()

        // Verify that the UserGuideFragment is loaded
        Mockito.verify(mockNavigationListener).loadFragment(Mockito.any(), Mockito.eq(FragmentTransaction.TRANSIT_FRAGMENT_OPEN))
    }

    @Test
    fun testQuitButtonClick() {
        // Simulate Quit button click
        landingPageFragment.binding.quitbutton.performClick()

        // Verify that the activity finishes when the quit button is clicked
        Mockito.verify(mockContext).finish()
    }

    @Test
    fun testOnPause_CleanUp() {
        landingPageFragment.onPause()

        // Verify that music is paused and TTS is stopped
        Mockito.verify(mockBackgroundMusicPlayer).pauseMusic()
        Mockito.verify(mockTTSUtility).stop()
    }

    @Test
    fun testOnDestroyView_CleanUp() {
        landingPageFragment.onDestroyView()

        // Verify that music is stopped and TTS is shut down
        Mockito.verify(mockBackgroundMusicPlayer).stopMusic()
        Mockito.verify(mockTTSUtility).shutdown()

        // Verify that the toolbar is made visible again
        Mockito.verify(mockContext).findViewById<MaterialToolbar>(R.id.toolbar)
    }

    @Test
    fun testOnDetach_ClearNavigationListener() {
        // Ensure that navigationListener is cleared in onDetach
        landingPageFragment.onDetach()
        assertNull(landingPageFragment.navigationListener)
    }
}
