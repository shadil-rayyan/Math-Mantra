package com.zendalona.zmantra

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class UserGuideFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // Test to check if the User Guide content is displayed
    @Test
    fun testUserGuideContentDisplayed() {
        // Verify that the content is displayed correctly
        onView(withId(R.id.llUserGuideContent))
            .check(matches(isDisplayed()))

        // Verify that some expected text (from `user_guide_text` resource) is displayed in the User Guide
        onView(withText("Sample Guide Text")).check(matches(isDisplayed()))
    }

    // Test to check if the Go Back button is displayed
    @Test
    fun testGoBackButtonDisplayed() {
        // Verify that the Go Back button is visible
        onView(withId(R.id.goBackButton)).check(matches(isDisplayed()))
    }

    // Test to check the functionality of the Go Back button
    @Test
    fun testGoBackButtonClick() {
        // Click on the Go Back button
        onView(withId(R.id.goBackButton)).perform(click())

        // You can add assertions here to verify that the fragment is popped or the activity is closed
        // Example: Verify that the fragment is removed or the previous activity is shown.
    }
}
