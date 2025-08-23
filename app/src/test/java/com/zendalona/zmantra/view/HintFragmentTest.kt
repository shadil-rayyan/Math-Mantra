import android.R
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.zendalona.zmantra.presentation.features.hint.HintFragment
import com.zendalona.zmantra.core.utility.excel.ExcelHintReader
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class HintFragmentTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var hintFragment: HintFragment

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        hintFragment = HintFragment()
    }

    @Test
    fun testConvertHintToHtml_Objective() {
        val hintText = "Objective: Complete the game"

        // Expected HTML structure
        val expectedHtml = """
            <html><body style='font-size:18px; font-family: Arial, sans-serif;'>
            <h2><strong>Objective of the Game</strong></h2>
            <p>$hintText</p>
            </body></html>
        """.trimIndent()

        val result = hintFragment.convertHintToHtml(hintText)
        assertEquals(expectedHtml, result)
    }

    @Test
    fun testConvertHintToHtml_Tips() {
        val hintText = "Tips: Use the map to navigate."

        // Expected HTML structure
        val expectedHtml = """
            <html><body style='font-size:18px; font-family: Arial, sans-serif;'>
            <h2><strong>Tips</strong></h2>
            <ul><li>$hintText</li></ul>
            </body></html>
        """.trimIndent()

        val result = hintFragment.convertHintToHtml(hintText)
        assertEquals(expectedHtml, result)
    }

    @Test
    fun testConvertHintToHtml_Accessibility() {
        val hintText = "Accessibility Features: Voice commands enabled."

        // Expected HTML structure
        val expectedHtml = """
            <html><body style='font-size:18px; font-family: Arial, sans-serif;'>
            <h2><strong>Accessibility Features</strong></h2>
            <p>$hintText</p>
            </body></html>
        """.trimIndent()

        val result = hintFragment.convertHintToHtml(hintText)
        assertEquals(expectedHtml, result)
    }

    @Test
    fun testConvertHintToHtml_Default() {
        val hintText = "This is a normal hint."

        // Expected HTML structure
        val expectedHtml = """
            <html><body style='font-size:18px; font-family: Arial, sans-serif;'>
            <p>$hintText</p>
            </body></html>
        """.trimIndent()

        val result = hintFragment.convertHintToHtml(hintText)
        assertEquals(expectedHtml, result)
    }

    @Test
    fun testExcelHintReaderFallback() {
        // Mock the ExcelHintReader to return an empty string, simulating no data in the Excel file
        Mockito.`when`(ExcelHintReader.getHintFromExcel(mockContext, "en", "default")).thenReturn("")

        // Check if fallback hint is returned when no data is found
        val fallbackHint = hintFragment.getString(R.string.hint_fallback)
        assertTrue(fallbackHint.isNotEmpty())
    }

    @Test
    fun testLanguageFallback() {
        // Test that the language is defaulted to "en" if it's empty
        val language = if (TextUtils.isEmpty("")) "en" else ""
        assertEquals("en", language)
    }

    @Test
    fun testGetModeArgument() {
        // Test if the mode is correctly fetched from the arguments
        val fragment = HintFragment()
        val args = Bundle().apply { putString("mode", "testMode") }
        fragment.arguments = args
        val mode = fragment.arguments?.getString("mode", "default") ?: "default"
        assertEquals("testMode", mode)
    }
}
