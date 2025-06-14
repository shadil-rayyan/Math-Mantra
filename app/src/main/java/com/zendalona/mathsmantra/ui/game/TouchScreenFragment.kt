package com.zendalona.mathsmantra.ui.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameTouchScreenBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.mathsmantra.utility.common.*
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class TouchScreenFragment : Fragment(), Hintable {

    private var binding: FragmentGameTouchScreenBinding? = null
    private lateinit var tts: TTSUtility
    private val handler = Handler(Looper.getMainLooper())

    private var index = 0
    private var wrongAttempts = 0
    private var questionStartTime = 0L
    private var correctAnswer = 0
    private var inputLocked = false

    private lateinit var lang: String
    private lateinit var difficulty: String
    private lateinit var parsedTouchList: List<TouchQuestion>

    data class TouchQuestion(
        val expression: String,
        val answer: Int,
        val timeLimit: Int,
        val celebration: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TTSUtility(requireContext())
        lang = LocaleHelper.getLanguage(requireContext())
        difficulty = DifficultyPreferences.getDifficulty(requireContext())
        parsedTouchList = loadTouchQuestionsFromAssets(lang, difficulty)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameTouchScreenBinding.inflate(inflater, container, false)
        startGame()
        return binding!!.root
    }

    private fun loadTouchQuestionsFromAssets(lang: String, difficulty: String): List<TouchQuestion> {
        val questions = mutableListOf<TouchQuestion>()
        val fileName = "$lang/game/touchTheScreen/${difficulty.lowercase(Locale.ROOT)}.txt"

        try {
            val reader = BufferedReader(InputStreamReader(requireContext().assets.open(fileName)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    val parts = it.split("→")
                    val (expression, answer) = QuestionParser.parseExpression(parts[0])
                    val timeLimit = parts.getOrNull(1)?.toIntOrNull() ?: 10
                    val celebration = parts.getOrNull(2)?.toIntOrNull() == 1
                    questions.add(TouchQuestion(expression, answer, timeLimit, celebration))
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading touch questions: $fileName", Toast.LENGTH_SHORT).show()
        }

        return questions
    }

    private fun startGame() {
        if (index >= parsedTouchList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
            return
        }

        inputLocked = false
        val question = parsedTouchList[index]
        correctAnswer = question.answer
        questionStartTime = System.currentTimeMillis()

        val readableExpr = question.expression.replace("+", " plus ").replace("-", " minus ")
        val speakText = "Touch the screen with $readableExpr fingers"

        binding?.angleQuestion?.apply {
            text = speakText
            contentDescription = speakText
            announceForAccessibility(speakText)
        }

        tts.speak(speakText)
        setupTouchListener()
    }

    private fun setupTouchListener() {
        binding?.root?.setOnTouchListener { _, event ->
            val pointerCount = event.pointerCount

            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                binding?.angleQuestion?.text =
                    getString(R.string.touchscreen_fingers_on_screen, pointerCount)

                if (pointerCount == correctAnswer && !inputLocked) {
                    inputLocked = true
                    evaluateGameResult(success = true)
                }
            }

            if (event.action == MotionEvent.ACTION_UP && !inputLocked) {
                inputLocked = true
                evaluateGameResult(success = false)
            }
            true
        }
    }

    private fun evaluateGameResult(success: Boolean) {
        handler.postDelayed({
            val question = parsedTouchList[index]
            val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
            val grade = GradingUtils.getGrade(elapsedSeconds, question.timeLimit.toDouble(), success)

            if (success) {
                wrongAttempts = 0
                if (question.celebration) {
                    MediaPlayer.create(context, R.raw.bell_ring)?.apply {
                        setOnCompletionListener { release() }
                        start()
                    }
                }
                DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                    index++
                    startGame()
                }

            } else {
                wrongAttempts++
                if (wrongAttempts >= 3) {
                    tts.speak(getString(R.string.shake_game_over))
                    endGameWithScore()
                } else {
                    DialogUtils.showRetryDialog(
                        requireContext(),
                        layoutInflater,
                        tts,
                        getString(R.string.shake_failure)
                    ) {
                        startGame()
                    }
                }
            }

        }, 500)
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/touch.txt")
        }
        val hintFragment = HintFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()

        val service = AccessibilityHelper.getAccessibilityService()
        if (service != null) {
            AccessibilityHelper.disableExploreByTouch(service)
        }
    }

    override fun onPause() {
        super.onPause()
        val service = AccessibilityHelper.getAccessibilityService()
        if (service != null) {
            AccessibilityHelper.resetExploreByTouch(service)
        }
        handler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
        binding = null
    }
}
