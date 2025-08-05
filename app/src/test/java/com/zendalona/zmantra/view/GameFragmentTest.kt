import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentTransaction
import com.zendalona.zmantra.presentation.features.game.GameFragment
import com.zendalona.zmantra.presentation.features.landing.FragmentNavigation
import com.zendalona.zmantra.view.game.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

class GameFragmentTest {

    @Mock
    private lateinit var mockNavigationListener: FragmentNavigation

    private lateinit var gameFragment: GameFragment

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)  // For older Mockito versions (prior to 3.x)
        gameFragment = GameFragment()

        // Simulate onAttach to set the mock navigation listener
        gameFragment.onAttach(mock(Context::class.java).apply {
            Mockito.`when`(this is FragmentNavigation).thenReturn(true)
        })
        gameFragment.onAttach(mockNavigationListener as Activity)
    }

    @Test
    fun testShouldShowHintIcon() {
        // Test that hint icon visibility is false
        assert(!gameFragment.shouldShowHintIcon())
    }

    @Test
    fun testShakeButtonClick() {
        // Simulate button click
        gameFragment.binding!!.shakeButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            ShakeFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testTapButtonClick() {
        // Simulate button click
        gameFragment.binding!!.tapButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            TapFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testAngleButtonClick() {
        // Simulate button click
        gameFragment.binding!!.angleButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            AngleFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testDrawingButtonClick() {
        // Simulate button click
        gameFragment.binding!!.drawingButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            DrawingFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testDirectionButtonClick() {
        // Simulate button click
        gameFragment.binding!!.directionButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            CompassFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testNumberLineButtonClick() {
        // Simulate button click
        gameFragment.binding!!.numberlineButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            NumberLineFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testStereoSoundButtonClick() {
        // Simulate button click
        gameFragment.binding!!.stereoSoundButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            SterioFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testMentalCalculationButtonClick() {
        // Simulate button click
        gameFragment.binding!!.mentalCalculationButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            MentalCalculationFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testTouchTheScreenButtonClick() {
        // Simulate button click
        gameFragment.binding!!.touchTheScreenColorPrimary.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            TouchScreenFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }

    @Test
    fun testDayButtonClick() {
        // Simulate button click
        gameFragment.binding!!.dayButton.performClick()

        // Verify if correct fragment was loaded
        Mockito.verify(mockNavigationListener).loadFragment(
            DayFragment(),
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        )
    }
}
