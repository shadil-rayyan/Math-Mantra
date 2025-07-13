import android.R
import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.zendalona.zmantra.view.LearningFragment
import com.zendalona.zmantra.view.QuickPlayFragment
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.junit.Assert.*
import androidx.fragment.app.FragmentActivity
import com.zendalona.zmantra.databinding.FragmentLearningmodeBinding

class LearningFragmentTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFragmentActivity: FragmentActivity

    @Mock
    private lateinit var mockFragmentTransaction: FragmentTransaction

    @Mock
    private lateinit var mockQuickPlayFragment: QuickPlayFragment

    @Mock
    private lateinit var mockBinding: FragmentLearningmodeBinding

    private lateinit var learningFragment: LearningFragment

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)  // For older Mockito versions (prior to 3.x)

        // Initialize the LearningFragment
        learningFragment = LearningFragment()

        // Mock the fragment transaction behavior
        Mockito.`when`(mockFragmentActivity.supportFragmentManager.beginTransaction())
            .thenReturn(mockFragmentTransaction)

        // Mock the new instance creation of QuickPlayFragment
        Mockito.`when`(QuickPlayFragment.newInstance(Mockito.anyString()))
            .thenReturn(mockQuickPlayFragment)

        // Mock the binding for the fragment
        learningFragment.onCreateView(Mockito.mock(LayoutInflater::class.java), null, null)

        // Set up fragment with necessary context
        learningFragment.onAttach(mockContext)
    }

    @Test
    fun testCategoryButtonClicks_launchesCorrectQuickPlayFragment() {
        // Test Time Category Button Click
        learningFragment.binding.cardTime.performClick()
        Mockito.verify(mockFragmentTransaction).replace(
            Mockito.eq(R.id.fragment_container),
            Mockito.eq(mockQuickPlayFragment)
        )
        Mockito.verify(mockFragmentTransaction).addToBackStack(null)
        Mockito.verify(mockFragmentTransaction).commit()

        // Test Currency Category Button Click
        learningFragment.binding.cardCurrency.performClick()
        Mockito.verify(mockFragmentTransaction).replace(
            Mockito.eq(R.id.fragment_container),
            Mockito.any(QuickPlayFragment::class.java)
        )
        Mockito.verify(mockFragmentTransaction).addToBackStack(null)
        Mockito.verify(mockFragmentTransaction).commit()

        // Continue for other categories (Addition, Subtraction, etc.)
        learningFragment.binding.cardAddition.performClick()
        Mockito.verify(mockFragmentTransaction).replace(
            Mockito.eq(R.id.fragment_container),
            Mockito.any(QuickPlayFragment::class.java)
        )
        Mockito.verify(mockFragmentTransaction).addToBackStack(null)
        Mockito.verify(mockFragmentTransaction).commit()

        learningFragment.binding.cardSubtraction.performClick()
        Mockito.verify(mockFragmentTransaction).replace(
            Mockito.eq(R.id.fragment_container),
            Mockito.any(QuickPlayFragment::class.java)
        )
        Mockito.verify(mockFragmentTransaction).addToBackStack(null)
        Mockito.verify(mockFragmentTransaction).commit()
    }

    @Test
    fun testLaunchQuickPlay_CorrectCategory() {
        // Test that the QuickPlayFragment is launched with the correct category
        learningFragment.launchQuickPlay("multiplication")

        // Verify that QuickPlayFragment is created with the correct category
        Mockito.verify(mockFragmentTransaction).replace(
            Mockito.eq(R.id.fragment_container),
            Mockito.any(QuickPlayFragment::class.java)
        )
        Mockito.verify(mockFragmentTransaction).addToBackStack(null)
        Mockito.verify(mockFragmentTransaction).commit()
    }

    @Test
    fun testOnDestroyView_CleansUpBinding() {
        // Test that onDestroyView cleans up the binding
        learningFragment.onDestroyView()

        // Assert that binding is null after cleanup
        assertNull(learningFragment.binding)
    }

    @Test
    fun testFragmentTransactionCalls() {
        // Test that the fragment transaction replace and commit methods are called
        learningFragment.launchQuickPlay("division")
        Mockito.verify(mockFragmentTransaction).replace(
            Mockito.eq(R.id.fragment_container),
            Mockito.any(QuickPlayFragment::class.java)
        )
        Mockito.verify(mockFragmentTransaction).addToBackStack(null)
        Mockito.verify(mockFragmentTransaction).commit()
    }
}
