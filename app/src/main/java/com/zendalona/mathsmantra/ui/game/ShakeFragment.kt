package com.zendalona.mathsmantra.ui.game

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameShakeBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.AccelerometerUtility
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import com.zendalona.mathsmantra.utility.common.*
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class ShakeFragment : Fragment(), Hintable {

    private var binding: FragmentGameShakeBinding? = null
    private lateinit var accelerometerUtility: AccelerometerUtility
    private lateinit var tts: TTSUtility
    private var count = 0
    private var target = 0
    private var isShakingAllowed = true
    private val shakeHandler = Handler()
    private val gameHandler = Handler(Looper.getMainLooper())
    private var index = 0

    private var retryCount = 0
    private var failCountOnQuestion = 0
    private var totalFailedQuestions = 0
    private var firstShakeTime: Long = 0
    private var answerChecked = false

    private lateinit var lang: String
    private lateinit var difficulty: String

    private data class ShakeQuestion(
        val expression: String,
        val answer: Int,
        val timeLimit: Int = 20,
        val celebration: Boolean = false
    )

    private var parsedShakeList: List<ShakeQuestion> = emptyList()
    private var questionStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = LocaleHelper.getLanguage(requireContext())
        difficulty = DifficultyPreferences.getDifficulty(requireContext())

        tts = TTSUtility(requireContext())
        accelerometerUtility = AccelerometerUtility(requireContext())
        parsedShakeList = loadShakeQuestionsFromAssets(lang, difficulty)
        if (parsedShakeList.isEmpty()) {
            parsedShakeList = listOf(
                ShakeQuestion("2+1", 3),
                ShakeQuestion("1+2+3", 6)
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameShakeBinding.inflate(inflater, container, false)
        startGame()
        return binding!!.root
    }

    private fun loadShakeQuestionsFromAssets(lang: String, difficulty: String): List<ShakeQuestion> {
        val questions = mutableListOf<ShakeQuestion>()
        val fileName = "$lang/game/shake/${difficulty.lowercase(Locale.ROOT)}.txt"

        try {
            val reader = BufferedReader(InputStreamReader(requireContext().assets.open(fileName)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    val parts = it.split("→")
                    val (questionText, answer) = QuestionParser.parseExpression(parts[0])
                    val timeLimit = parts.getOrNull(1)?.toIntOrNull() ?: 20
                    val celebration = parts.getOrNull(2)?.toIntOrNull() == 1
                    questions.add(ShakeQuestion(questionText, answer, timeLimit, celebration))
                }
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Error loading shake questions: $fileName", Toast.LENGTH_SHORT).show()
        }

        return questions
    }

    private fun startGame() {
        count = 0
        firstShakeTime = 0L
        answerChecked = false
        binding?.ringCount?.text = getString(R.string.shake_count_initial)

        val question = parsedShakeList[index % parsedShakeList.size]
        target = question.answer
        questionStartTime = System.currentTimeMillis()

        // Visual + TTS Instruction
        val instruction = getString(R.string.shake_target_expression, question.expression)
        val speakText = question.expression.replace("+", " plus ")
        val speakInstruction = "Shake $speakText times"

        binding?.ringMeTv?.apply {
            text = instruction
            contentDescription = speakInstruction
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            isFocusable = true
            isFocusableInTouchMode = true
            postDelayed({ requestFocus(); announceForAccessibility(speakInstruction) }, 500)
        }

        tts.speak(speakInstruction)
    }

    private fun onShakeDetected() {
        if (!isShakingAllowed) return

        isShakingAllowed = false
        shakeHandler.postDelayed({ isShakingAllowed = true }, 500)

        count++
        binding?.ringCount?.text = count.toString()

        tts.stop()
        val countText = getString(R.string.shake_count_announcement, count)
        tts.speak(countText)

        if (firstShakeTime == 0L) {
            firstShakeTime = System.currentTimeMillis()
            answerChecked = false

            // Schedule 2-second timer to evaluate answer
            gameHandler.postDelayed({
                checkAnswer()
            }, 3000)
        }

        // If count already above target and answer not checked, force wrong answer immediately
        if (count > target && !answerChecked) {
            checkAnswer(forceWrong = true)
        }
    }

    private fun checkAnswer(forceWrong: Boolean = false) {
        if (answerChecked) return

        val question = parsedShakeList[index]
        answerChecked = true

        // If forced wrong or count > target → wrong answer
        if (forceWrong || count > target) {
            retryCount++
            failCountOnQuestion++

            if (failCountOnQuestion >= 6) {
                // Move to next question after 6 fails on current question
                totalFailedQuestions++
                retryCount = 0
                failCountOnQuestion = 0

                if (totalFailedQuestions >= 3) {
                    tts.speak(getString(R.string.shake_game_over))
                    endGameWithScore()
                    return
                } else {
                    val correctAnswerMessage = "${question.expression} = ${question.answer}"
                    DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater, tts, correctAnswerMessage) {
                        nextOrEnd()
                    }
                    return
                }
            }

            if (retryCount >= 3) {
                // Show correct answer dialog but DO NOT reload question or move next
                retryCount = 0
                val correctAnswerMessage = "${question.expression} = ${question.answer}"
                DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater, tts, correctAnswerMessage) {}
            } else {
                // Show retry dialog to allow another attempt
                DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.shake_failure)) {
                    // Reset for retry
                    count = 0
                    firstShakeTime = 0L
                    answerChecked = false
                    binding?.ringCount?.text = getString(R.string.shake_count_initial)
                    val speakText = question.expression.replace("+", " plus ")
                    val speakInstruction = "Shake $speakText times"
                    tts.speak(speakInstruction)
                }
            }

        } else if (count == target) {
            // Correct answer
            retryCount = 0
            failCountOnQuestion = 0
            totalFailedQuestions = 0

            if (question.celebration) {
                MediaPlayer.create(context, R.raw.bell_ring)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }

            val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
            val grade = GradingUtils.getGrade(elapsedSeconds, question.timeLimit.toDouble(), true)

            DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                nextOrEnd()
            }
        } else {
            // count < target, do nothing (allow user to keep shaking)
            // Reset answerChecked to false so checkAnswer can be called again later if needed
            answerChecked = false
        }

        // Reset count and timers only if answer is correct or forced wrong
        if (answerChecked) {
            count = 0
            firstShakeTime = 0L
        }
    }


    private fun nextOrEnd() {
        index++
        if (index >= parsedShakeList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
        } else {
            tts.speak(getString(R.string.shake_next_question))
            gameHandler.postDelayed({ startGame() }, 1000)
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/shake.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        accelerometerUtility.registerListener()
        isShakingAllowed = true
        shakeHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isVisible && accelerometerUtility.isDeviceShaken()) {
                    requireActivity().runOnUiThread { onShakeDetected() }
                }
                shakeHandler.postDelayed(this, 500)
            }
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        accelerometerUtility.unregisterListener()
        shakeHandler.removeCallbacksAndMessages(null)
        gameHandler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        tts.shutdown()
    }
}
