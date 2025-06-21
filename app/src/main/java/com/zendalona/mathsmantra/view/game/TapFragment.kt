package com.zendalona.mathsmantra.view.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.MotionEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameTapBinding
import com.zendalona.mathsmantra.model.GameQuestion
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.mathsmantra.utility.common.*
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import com.zendalona.mathsmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.mathsmantra.view.HintFragment

class TapFragment : Fragment(), Hintable {

    private var binding: FragmentGameTapBinding? = null
    private lateinit var tts: TTSUtility
    private val handler = Handler(Looper.getMainLooper())

    private var index = 0
    private var count = 0
    private var retryCount = 0
    private var failCountOnQuestion = 0
    private var totalFailedQuestions = 0
    private var questionStartTime: Long = 0
    private var answerCheckScheduled = false

    private var questions: List<GameQuestion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TTSUtility(requireContext())

        val lang = LocaleHelper.getLanguage(requireContext())
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())

        questions = ExcelQuestionLoader.loadQuestionsFromExcel(
            requireContext(),
            lang,
            "tap",
            difficulty.toString()
        )

//        if (questions.isEmpty()) {
//            questions = listOf(
//                GameQuestion("2+1", 3),
//                GameQuestion("1+2+3", 6)
//            )
//        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameTapBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding?.root?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                onTap()
            }
            true
        }

        startGame()
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun startGame() {
        if (index >= questions.size) {
            tts.speak(getString(R.string.tap_game_over))
            endGameWithScore()
            return
        }

        count = 0
        retryCount = 0
        failCountOnQuestion = 0
        answerCheckScheduled = false
        binding?.tapCount?.text = "0"

        val question = questions[index]
        questionStartTime = System.currentTimeMillis()

        val instructionText = getString(R.string.tap_target_expression, question.expression)
        val speakInstruction = "Tap ${question.expression.replace("+", " plus ")}"

        binding?.tapMeTv?.apply {
            text = instructionText
            contentDescription = speakInstruction
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            isFocusable = true
            isFocusableInTouchMode = true
            postDelayed({ requestFocus(); announceForAccessibility(speakInstruction) }, 500)
        }

        tts.speak(speakInstruction)
    }

    private fun onTap() {
        count++
        binding?.tapCount?.text = count.toString()
        VibrationUtils.vibrate(requireContext(), 100)

        tts.stop()
        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            tts.speak(getString(R.string.tap_count_announcement, count))
        }

        val question = questions[index]

        if (count == question.answer && !answerCheckScheduled) {
            answerCheckScheduled = true

            handler.postDelayed({
                if (count == question.answer) {
                    checkAnswer(true)
                } else if (count > question.answer) {
                    checkAnswer(false)
                }
            }, 3000)

        } else if (count > question.answer && !answerCheckScheduled) {
            checkAnswer(false)
        }
    }

    private fun checkAnswer(isCorrect: Boolean) {
        val question = questions[index]
        val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val grade = GradingUtils.getGrade(elapsedSeconds, question.timeLimit.toDouble(), isCorrect)

        if (isCorrect) {
            if (question.celebration) {
                MediaPlayer.create(requireContext(), R.raw.bell_ring)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }

            DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                nextQuestion()
            }

        } else {
            retryCount++
            failCountOnQuestion++

            if (failCountOnQuestion >= 6) {
                totalFailedQuestions++
                retryCount = 0
                failCountOnQuestion = 0

                if (totalFailedQuestions >= 3) {
                    tts.speak(getString(R.string.tap_game_over))
                    endGameWithScore()
                } else {
                    DialogUtils.showNextDialog(requireContext(), layoutInflater, tts, getString(R.string.moving_to_next_question)) {
                        nextQuestion()
                    }
                }
                return
            }

            if (retryCount >= 3) {
                retryCount = 0
                DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater, tts, question.answer.toString()) {
                    resetQuestion()
                }
            } else {
                DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.tap_failure)) {
                    resetQuestion()
                }
            }
        }
    }

    private fun resetQuestion() {
        count = 0
        answerCheckScheduled = false
        binding?.tapCount?.text = "0"

        val question = questions[index]
        val speakInstruction = "Tap ${question.expression.replace("+", " plus ")}"
        tts.speak(speakInstruction)
    }

    private fun nextQuestion() {
        index++
        startGame()
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "tap")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.disableExploreByTouch(it)
        }
    }

    override fun onPause() {
        super.onPause()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.resetExploreByTouch(it)
        }
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        handler.removeCallbacksAndMessages(null)
        tts.shutdown()
    }
}
