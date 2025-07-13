package com.zendalona.zmantra.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.platform.app.InstrumentationRegistry
import com.zendalona.zmantra.view.LandingPageFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not


@RunWith(AndroidJUnit4::class)
class LandingPageTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java) // Use the correct main activity

    // Helper function to launch the LandingPageFragment
    private fun launchLandingPageFragment() {
        val landingFragment = LandingPageFragment()
        activityRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, landingFragment)
                .commitNow()
        }
    }

    @Test
    fun testLandingPageButtonsDisplayed() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Verify that all buttons are visible
        onView(withId(R.id.quickplay)).check(matches(isDisplayed()))
        onView(withId(R.id.learningButton)).check(matches(isDisplayed()))
        onView(withId(R.id.GameButton)).check(matches(isDisplayed()))
        onView(withId(R.id.userGuide)).check(matches(isDisplayed()))
        onView(withId(R.id.settings)).check(matches(isDisplayed()))
        onView(withId(R.id.quitbutton)).check(matches(isDisplayed()))
    }

    @Test
    fun testQuickplayButtonClick() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Click the "Quickplay" button
        onView(withId(R.id.quickplay)).perform(click())

        // Verify that the navigation happens, i.e., QuickPlayFragment is loaded
        // Assuming we use FragmentTransaction.TRANSIT_FRAGMENT_OPEN for the transition
        // You would want to assert that the fragment has been loaded
        onView(withId(R.id.quickplay)).check(matches(isDisplayed()))  // This is a placeholder
    }

    @Test
    fun testLearningButtonClick() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Click the "Learning" button
        onView(withId(R.id.learningButton)).perform(click())

        // Verify that the navigation happens to LearningFragment
        // Check for the expected content of the LearningFragment here (e.g., a unique view)
        onView(withId(R.id.learningButton)).check(matches(isDisplayed()))  // This is a placeholder
    }

    @Test
    fun testGameButtonClick() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Click the "Game" button
        onView(withId(R.id.GameButton)).perform(click())

        // Verify that the navigation happens to GameFragment
        onView(withId(R.id.GameButton)).check(matches(isDisplayed()))  // This is a placeholder
    }

    @Test
    fun testUserGuideButtonClick() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Click the "User Guide" button
        onView(withId(R.id.userGuide)).perform(click())

        // Verify that the navigation happens to UserGuideFragment
        onView(withId(R.id.userGuide)).check(matches(isDisplayed()))  // This is a placeholder
    }

    @Test
    fun testSettingsButtonClick() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Click the "Settings" button
        onView(withId(R.id.settings)).perform(click())

        // Verify that the navigation happens to SettingFragment
        onView(withId(R.id.settings)).check(matches(isDisplayed()))  // This is a placeholder
    }

    @Test
    fun testQuitButtonClick() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Click the "Quit" button
        onView(withId(R.id.quitbutton)).perform(click())

        // Verify that the activity finishes
        // You might need to check that the activity has been closed by asserting that
        // the app's package is no longer running or by using a mock for the activity
        // and verifying that `finish()` has been called
    }

    @Test
    fun testFooterTextDisplayed() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Check that the footer text is displayed
        onView(withId(R.id.footerText)).check(matches(isDisplayed()))
    }

    @Test
    fun testToolbarHiddenOnLandingPage() {
        // Launch the LandingPageFragment
        launchLandingPageFragment()

        // Check if the toolbar is hidden on this fragment
        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))
    }
}
