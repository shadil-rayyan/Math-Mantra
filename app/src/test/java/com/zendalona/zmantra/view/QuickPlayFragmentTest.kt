package com.zendalona.zmantra.view

import android.content.Context
import android.media.MediaPlayer
import androidx.fragment.app.FragmentActivity
import com.zendalona.zmantra.domain.model.GameQuestion
import com.zendalona.zmantra.core.utility.common.TTSUtility
import com.zendalona.zmantra.core.utility.excel.ExcelQuestionLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.junit.Assert.*

class QuickPlayFragmentTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFragmentActivity: FragmentActivity

    @Mock
    private lateinit var mockExcelQuestionLoader: ExcelQuestionLoader

    @Mock
    private lateinit var mockMediaPlayer: MediaPlayer

    @Mock
    private lateinit var mockTTSUtility: TTSUtility

    private lateinit var quickPlayFragment: QuickPlayFragment

    private val testQuestionList = listOf(
        GameQuestion(expression = "2 + 2", answer = 4, timeLimit = 10),
        GameQuestion(expression = "5 - 3", answer = 2, timeLimit = 10)
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)  // For older Mockito versions (prior to 3.x)

        quickPlayFragment = QuickPlayFragment().apply {
            // Simulating Fragment's context and setup
            context = mockContext
        }

        // Mocking the required context behavior for loading questions
        Mockito.`when`(ExcelQuestionLoader.loadQuestionsFromExcel(
            context = mockContext,
            lang = "en",
            mode = "default",
            difficulty = "1"
        )).thenReturn(testQuestionList)

        // Mocking MediaPlayer
        Mockito.`when`(MediaPlayer.create(mockContext, Mockito.anyInt())).thenReturn(mockMediaPlayer)

        // Mocking TTS Utility
        Mockito.`when`(mockTTSUtility.speak(Mockito.anyString())).thenReturn(Unit)
    }

    @Test
    fun testLoadQuestions() {
        // Simulating the question loading behavior
        runBlocking(Dispatchers.Main) {
            quickPlayFragment.onCreate(null)
            assertEquals(testQuestionList.size, quickPlayFragment.questionList.size)
            assertEquals("2 + 2", quickPlayFragment.questionList[0].expression)
        }
    }

    @Test
    fun testCorrectAnswerHandling() {
        // Simulate the game flow: User answers correctly

        // Load questions
        runBlocking(Dispatchers.Main) {
            quickPlayFragment.onCreate(null)
            quickPlayFragment.loadNextQuestion()
        }

        val question = quickPlayFragment.questionList[0]
        val correctAnswer = question.answer
        quickPlayFragment.binding.answerEt.setText(correctAnswer.toString())

        // Simulate clicking the submit button
        quickPlayFragment.checkAnswer()

        // Assert that the score increases
        assertTrue(quickPlayFragment.totalScore > 0)
    }

    @Test
    fun testWrongAnswerHandling() {
        // Simulate the game flow: User answers incorrectly

        // Load questions
        runBlocking(Dispatchers.Main) {
            quickPlayFragment.onCreate(null)
            quickPlayFragment.loadNextQuestion()
        }

        val question = quickPlayFragment.questionList[0]
        val wrongAnswer = question.answer + 1 // Just an incorrect answer
        quickPlayFragment.binding.answerEt.setText(wrongAnswer.toString())

        // Simulate clicking the submit button
        quickPlayFragment.checkAnswer()

        // Assert that the number of attempts increases
        assertTrue(quickPlayFragment.currentQuestionAttempts == 1)
    }

    @Test
    fun testMaxAttemptsWrongAnswer() {
        // Simulate the scenario where the user reaches the maximum number of attempts

        // Load questions
        runBlocking(Dispatchers.Main) {
            quickPlayFragment.onCreate(null)
            quickPlayFragment.loadNextQuestion()
        }

        val question = quickPlayFragment.questionList[0]
        val wrongAnswer = question.answer + 1 // Incorrect answer
        quickPlayFragment.binding.answerEt.setText(wrongAnswer.toString())

        // Simulate 3 wrong attempts
        for (i in 1..3) {
            quickPlayFragment.checkAnswer()
        }

        // Assert that the wrong answer was added to the wrongQuestionsSet
        assertTrue(quickPlayFragment.wrongQuestionsSet.contains(0))
    }

    @Test
    fun testEndGame() {
        // Test if the game ends when no more questions are left or after a max number of wrong attempts

        // Load questions
        runBlocking(Dispatchers.Main) {
            quickPlayFragment.onCreate(null)
            quickPlayFragment.loadNextQuestion()
        }

        // Simulate end of the game (no more questions)
        quickPlayFragment.endGame()

        // Assert that the media player and TTSUtility are used to announce the end
        Mockito.verify(mockTTSUtility).speak(Mockito.anyString())
        Mockito.verify(mockMediaPlayer).release()
    }

    @Test
    fun testPlaySound() {
        // Test the correct sound is played on a correct/wrong answer

        // Test correct sound
        quickPlayFragment.playSound("correct")
        Mockito.verify(mockMediaPlayer).start()

        // Test wrong sound
        quickPlayFragment.playSound("wrong")
        Mockito.verify(mockMediaPlayer).start()
    }

    @Test
    fun testCleanupOnDestroyView() {
        // Test that resources are properly cleaned up

        quickPlayFragment.onDestroyView()

        // Assert that media player is released
        Mockito.verify(mockMediaPlayer).release()

        // Assert that TTSUtility is shut down
        Mockito.verify(mockTTSUtility).shutdown()
    }
}
