package com.zendalona.zmantra.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.platform.app.InstrumentationRegistry
import com.zendalona.zmantra.view.HintFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString

@RunWith(AndroidJUnit4::class)
class HintFragmentTest {

    @get:Rule
    var activityScenarioRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Launches the HintFragment with given mode and language
     */
    private fun launchHintFragment(mode: String, language: String) {
        val args = Bundle().apply {
            putString("mode", mode)
            putString("language", language)
        }

        val hintFragment = HintFragment().apply { arguments = args }

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, hintFragment)
                .commitNow()
        }
    }

    /**
     * Test to check if the WebView content is displayed correctly
     */
    @Test
    fun testHintContentDisplayed() {
        // Launch the HintFragment with a specific mode and language
        launchHintFragment("default", "en")

        // Check if the WebView is displayed
        onView(withId(R.id.webView))
            .check(matches(isDisplayed()))

        // Check if the "Go Back" button is visible
        onView(withId(R.id.goBackButton))
            .check(matches(isDisplayed()))
            .check(matches(withText("Go Back")))

        // Check if the WebView contains expected text
        onView(withId(R.id.webView)).check(matches(withText(containsString("Objective"))))
    }

    /**
     * Test for checking the functionality of the Go Back button
     */
    @Test
    fun testGoBackButtonFunctionality() {
        // Launch the HintFragment
        launchHintFragment("default", "en")

        // Perform a click action on the Go Back button
        onView(withId(R.id.goBackButton)).perform(click())

        // Check if the activity's back pressed is invoked (this would depend on your navigation flow)
        // You can also check for specific views appearing after the back action is performed
        // Example:
        // onView(withId(R.id.some_other_view)).check(matches(isDisplayed()))
    }

    /**
     * Test to check if the WebView renders the hint text correctly
     */
    @Test
    fun testHintTextRenderingInWebView() {
        // Launch the HintFragment with a language that has known content
        launchHintFragment("default", "en")

        // Check that the WebView displays the content
        onView(withId(R.id.webView)).check(matches(isDisplayed()))

        // Check that the WebView contains the expected HTML content
        onView(withId(R.id.webView))
            .check(matches(withText(containsString("Objective"))))
    }

    /**
     * Test to check if the fallback hint text appears when an invalid mode is passed
     */
    @Test
    fun testEmptyHintFallback() {
        // Launch the HintFragment with an invalid mode
        launchHintFragment("invalid_mode", "en")

        // Check if the fallback hint text is displayed
        onView(withId(R.id.webView)).check(matches(withText(containsString("This is the fallback hint"))))
    }
}
