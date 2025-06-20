package com.zendalona.mathsmantra.ui.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameShakeBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.model.GameQuestion
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.AccelerometerUtility
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.mathsmantra.utility.common.*
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper

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

    private var parsedShakeList: List<GameQuestion> = emptyList()
    private var questionStartTime: Long = 0

    companion object {
        private const val TAG = "ShakeFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        lang = LocaleHelper.getLanguage(requireContext())
        difficulty = DifficultyPreferences.getDifficulty(requireContext())
        Log.d(TAG, "Language: $lang, Difficulty: $difficulty")

        tts = TTSUtility(requireContext())
        accelerometerUtility = AccelerometerUtility(requireContext())
        parsedShakeList = ExcelQuestionLoader.loadQuestionsFromExcel(requireContext(), lang, "shake", difficulty)
        Log.d(TAG, "Loaded ${parsedShakeList.size} questions")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView called")
        binding = FragmentGameShakeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        startGame()
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun startGame() {
        Log.d(TAG, "Starting game at index: $index")

        if (parsedShakeList.isEmpty()) {
            Log.w(TAG, "No questions available")
            Toast.makeText(requireContext(), "No questions available", Toast.LENGTH_LONG).show()
            tts.speak("No questions available.")
            Handler(Looper.getMainLooper()).post {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            return
        }

        count = 0
        firstShakeTime = 0L
        answerChecked = false
        binding?.ringCount?.text = getString(R.string.shake_count_initial)

        val question = parsedShakeList[index % parsedShakeList.size]
        target = question.answer
        questionStartTime = System.currentTimeMillis()
        Log.d(TAG, "Question: ${question.expression}, Target: $target")

        val instruction = getString(R.string.shake_target_expression, question.expression)
        val speakText = question.expression.replace("+", " plus ")
        val speakInstruction = "Shake $speakText"

        binding?.ringMeTv?.apply {
            text = instruction
            contentDescription = speakInstruction
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            isFocusable = true
            postDelayed({ requestFocus(); announceForAccessibility(speakInstruction) }, 500)
        }

        tts.speak(speakInstruction)
    }

    private fun onShakeDetected() {
        if (!isShakingAllowed) {
            Log.d(TAG, "Shake ignored, not allowed yet")
            return
        }

        isShakingAllowed = false
        shakeHandler.postDelayed({ isShakingAllowed = true }, 500)

        count++
        Log.d(TAG, "Shake detected. Count: $count")
        binding?.ringCount?.text = count.toString()
        tts.stop()

        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            tts.speak(getString(R.string.shake_count_announcement, count))
        }

        if (firstShakeTime == 0L) {
            Log.d(TAG, "First shake. Starting timer to check answer")
            firstShakeTime = System.currentTimeMillis()
            answerChecked = false

        }

        // Immediately fail if count exceeds target
        if (count == target && !answerChecked) {
            Log.d(TAG, "Target reached. Checking answer immediately")
            checkAnswer()
        }

    }

    private fun checkAnswer(forceWrong: Boolean = false) {
        if (answerChecked) {
            Log.d(TAG, "Answer already checked")
            return
        }

        answerChecked = true
        val question = parsedShakeList[index % parsedShakeList.size]

        // Correct only if count == target and no forced wrong
        val isCorrect = !forceWrong && count == target

        if (isCorrect) {
            Log.d(TAG, "Correct answer")

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
            Log.d(TAG, "Answered in $elapsedSeconds seconds. Grade: $grade")

            DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                nextOrEnd()
            }
        } else {
            Log.w(TAG, "Wrong answer. Count: $count, Target: $target, ForceWrong: $forceWrong")

            retryCount++
            failCountOnQuestion++

            if (failCountOnQuestion >= 6) {
                totalFailedQuestions++
                Log.w(TAG, "6 failures on question. Total failed: $totalFailedQuestions")

                if (totalFailedQuestions >= 3) {
                    Log.e(TAG, "Game over due to too many failures")
                    tts.speak(getString(R.string.shake_game_over))
                    endGameWithScore()
                    return
                } else {
                    DialogUtils.showNextDialog(requireContext(), layoutInflater, tts, getString(R.string.moving_to_next_question)) {}
                    return
                }
            }

            if (retryCount >= 3) {
                Log.d(TAG, "Showing correct answer after 3 retries: ${question.answer}")
                DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater, tts, "${question.answer}") {}
            } else {
                DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.shake_failure)) {
                    Log.d(TAG, "Retrying current question")
                    count = 0
                    firstShakeTime = 0L
                    answerChecked = false
                    binding?.ringCount?.text = getString(R.string.shake_count_initial)
                    tts.speak("Shake ${question.expression.replace("+", " plus ")} times")
                }
            }
        }

        if (answerChecked) {
            count = 0
            firstShakeTime = 0L
        }
    }

    private fun nextOrEnd() {
        index++
        Log.d(TAG, "Next or end. New index: $index")

        if (index >= parsedShakeList.size) {
            Log.d(TAG, "All questions done. Ending game.")
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
        } else {
            tts.speak(getString(R.string.shake_next_question))
            gameHandler.postDelayed({ startGame() }, 1000)
        }
    }

    override fun showHint() {
        Log.d(TAG, "Hint requested")
        val bundle = Bundle().apply { putString("mode", "shake") }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
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
        Log.d(TAG, "onPause")
        accelerometerUtility.unregisterListener()
        shakeHandler.removeCallbacksAndMessages(null)
        gameHandler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        binding = null
        tts.shutdown()
    }
}
