package com.zendalona.zmantra.view

import android.os.Build
import android.text.Html
import android.util.Config
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentUserguideBinding
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
class UserGuideFragmentTest {

    private lateinit var userGuideFragment: UserGuideFragment
    private lateinit var mockBinding: FragmentUserguideBinding
    private lateinit var mockActivity: FragmentActivity

    @Before
    fun setup() {
        mockActivity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()

        // Mocking the Fragment and binding
        userGuideFragment = UserGuideFragment()
        mockBinding = Mockito.mock(FragmentUserguideBinding::class.java)
        userGuideFragment._binding = mockBinding

        // Mocking getString() to return a specific HTML string
        Mockito.`when`(mockActivity.getString(R.string.user_guide_text)).thenReturn("<p>Welcome to the User Guide!</p><br/><br/><p>Instructions here.</p>")
    }

    @Test
    fun `test onCreateView should split text into paragraphs and add TextViews`() {
        // Set up the mock fragment lifecycle
        val rawHtml = "<p>Welcome to the User Guide!</p><br/><br/><p>Instructions here.</p>"
        val paragraphs = rawHtml.split("<br/><br/>").map { it.trim() }.filter { it.isNotEmpty() }

        // Simulate onViewCreated method
        userGuideFragment.onViewCreated(userGuideFragment.view!!, null)

        // Verify that TextViews are created for each paragraph
        paragraphs.forEach { paragraph ->
            val textView = TextView(mockActivity)
            val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(paragraph, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(paragraph)
            }
            textView.text = spannedText

            verify(mockBinding.llUserGuideContent).addView(any(TextView::class.java))
        }
    }

    @Test
    fun `test goBackButton should call onBackPressed`() {
        // Simulate clicking the "go back" button
        userGuideFragment.binding.goBackButton.performClick()

        // Verify that onBackPressed is called on the activity
        verify(mockActivity).onBackPressed()
    }

    @Test
    fun `test shouldShowHintIcon returns false`() {
        // Verify that the method shouldShowHintIcon() returns false
        assert(userGuideFragment.shouldShowHintIcon() == false)
    }
}
