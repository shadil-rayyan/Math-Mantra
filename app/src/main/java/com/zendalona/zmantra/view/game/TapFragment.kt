package com.zendalona.zmantra.view.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameTapBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.zmantra.utility.common.TTSUtility

import com.zendalona.zmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.utility.common.*
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.view.HintFragment
import kotlinx.coroutines.launch

class TapFragment : Fragment(), Hintable {

    private var binding: FragmentGameTapBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tts: TTSUtility

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


        val lang = LocaleHelper.getLanguage(requireContext())
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())
        tts = TTSUtility(requireContext())

        lifecycleScope.launch {
            questions = ExcelQuestionLoader.loadQuestionsFromExcel(
                requireContext(),
                lang,
                "tap",
                difficulty.toString()
            )
            startGame()
        }
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

        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun startGame() {
        if (index >= questions.size) {
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

            // Request focus and announce the instruction after a short delay to ensure the view is fully loaded
            postDelayed({
                requestFocus()
                announceForAccessibility(speakInstruction)
            }, 500)
        }

        // Use announceForAccessibility for accessibility
        binding?.tapMeTv?.announceForAccessibility(speakInstruction)
    }

    private fun onTap() {
        count++
        binding?.tapCount?.text = count.toString()

        // Update contentDescription for accessibility (TalkBack will announce it)
        binding?.tapCount?.contentDescription = getString(R.string.tap_count_announcement, count)

        // Vibration feedback (good practice to confirm user action)
        VibrationUtils.vibrate(requireContext(), 100)

        // Announce the tap count for accessibility using announceForAccessibility
        binding?.tapCount?.announceForAccessibility(getString(R.string.tap_count_announcement, count))

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

            DialogUtils.showResultDialog(requireContext(), layoutInflater,tts, grade) {
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
                    endGameWithScore()
                } else {
                    DialogUtils.showNextDialog(requireContext(), layoutInflater,tts, getString(R.string.moving_to_next_question)) {
                        nextQuestion()
                    }
                }
                return
            }

            if (retryCount >= 3) {
                retryCount = 0
                DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater,tts, question.answer.toString()) {
                    resetQuestion()
                }
            } else {
                DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts,getString(R.string.tap_failure)) {
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
        val speakInstruction = getString(R.string.tap_target_expression, question.expression.replace("+", " plus "))

        // Use announceForAccessibility to make TalkBack announce the instruction
        binding?.tapMeTv?.apply {
            contentDescription = speakInstruction // Set the contentDescription for accessibility
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE // Make sure it announces on changes
            requestFocus() // Request focus to ensure accessibility services know which element to read
            postDelayed({
                announceForAccessibility(speakInstruction) // Announce the instruction to TalkBack
            }, 500)
        }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        handler.removeCallbacksAndMessages(null)
    }
}
