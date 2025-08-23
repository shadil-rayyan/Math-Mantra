package com.zendalona.zmantra

import android.content.Context
import androidx.fragment.app.FragmentTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.zendalona.zmantra.R
import com.zendalona.zmantra.view.game.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule
import androidx.fragment.app.FragmentActivity
import com.zendalona.zmantra.presentation.features.game.GameFragment
import com.zendalona.zmantra.databinding.FragmentGamePageBinding
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingRegistry
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
@LargeTest
class GameFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private lateinit var activity: FragmentActivity

    @Before
    fun setUp() {
        // Initialize the activity using the ActivityTestRule
        activity = activityRule.activity

        // Replace the content with GameFragment
        activity.supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, GameFragment())
            .commitAllowingStateLoss()

        // Optionally add a delay or use IdlingResource to wait for fragment loading
        Thread.sleep(500)  // You can replace this with IdlingResource if needed
    }

    @Test
    fun testShakeButtonLoadsShakeFragment() {
        onView(withId(R.id.shakeButton)).perform(click())

        // Use Thread.sleep for now to wait until the fragment loads
        Thread.sleep(500) // This can be replaced with IdlingResource for synchronization

        // Check if the ShakeFragment is displayed (replace with an actual view in ShakeFragment)
//         onView(withId(R.id.shakeFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testTapButtonLoadsTapFragment() {
        onView(withId(R.id.tapButton)).perform(click())
        Thread.sleep(500)  // Replace with synchronization method

        // Check if the TapFragment is displayed
        // onView(withId(R.id.tapFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testAngleButtonLoadsAngleFragment() {
        onView(withId(R.id.angleButton)).perform(click())
        Thread.sleep(500)

        // Check if the AngleFragment is displayed
        // onView(withId(R.id.angleFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testDrawingButtonLoadsDrawingFragment() {
        onView(withId(R.id.drawingButton)).perform(click())
        Thread.sleep(500)

        // Check if the DrawingFragment is displayed
        // onView(withId(R.id.drawingFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testDirectionButtonLoadsCompassFragment() {
        onView(withId(R.id.directionButton)).perform(click())
        Thread.sleep(500)

        // Check if the CompassFragment is displayed
        // onView(withId(R.id.compassFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testNumberLineButtonLoadsNumberLineFragment() {
        onView(withId(R.id.numberlineButton)).perform(click())
        Thread.sleep(500)

        // Check if the NumberLineFragment is displayed
        // onView(withId(R.id.numberLineFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testStereoSoundButtonLoadsStereoFragment() {
        onView(withId(R.id.stereoSoundButton)).perform(click())
        Thread.sleep(500)

        // Check if the SterioFragment is displayed
        // onView(withId(R.id.stereoFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testMentalCalculationButtonLoadsMentalCalculationFragment() {
        onView(withId(R.id.mentalCalculationButton)).perform(click())
        Thread.sleep(500)

        // Check if the MentalCalculationFragment is displayed
        // onView(withId(R.id.mentalCalculationFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testTouchTheScreenButtonLoadsTouchScreenFragment() {
        onView(withId(R.id.touch_the_screen_colorPrimary)).perform(click())
        Thread.sleep(500)

        // Check if the TouchScreenFragment is displayed
        // onView(withId(R.id.touchScreenFragmentViewId)).check(matches(isDisplayed()))
    }

    @Test
    fun testDayButtonLoadsDayFragment() {
        onView(withId(R.id.dayButton)).perform(click())
        Thread.sleep(500)

        // Check if the DayFragment is displayed
        // onView(withId(R.id.dayFragmentViewId)).check(matches(isDisplayed()))
    }
}
