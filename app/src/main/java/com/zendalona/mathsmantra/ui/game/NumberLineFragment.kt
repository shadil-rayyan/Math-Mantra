package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.zendalona.mathsmantra.Enum.Topic
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.DialogResultBinding
import com.zendalona.mathsmantra.databinding.FragmentGameNumberLineBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.RandomValueGenerator
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.common.GradingUtils
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.viewModel.NumberLineViewModel

private val handler = Handler(Looper.getMainLooper())

class NumberLineFragment : Fragment(), Hintable {

    companion object {
        private const val TAG = "NumberLineFragment"
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val MAX_WRONG_ATTEMPTS = 3
    }

    private var binding: FragmentGameNumberLineBinding? = null
    private lateinit var viewModel: NumberLineViewModel
    private var tts: TTSUtility? = null
    private var random: RandomValueGenerator? = null
    private var CURRENT_POSITION: String? = null
    private var answer = 0
    private var questionDesc = ""
    private var correctAnswerDesc = ""

    private lateinit var gestureDetector: GestureDetector

    private var answerCheckRunnable: Runnable? = null

    // Track wrong attempts per question key (e.g. start-end-answer string)
    private var wrongAttemptsForCurrentQuestion = 0
    private var lastQuestionKey: String? = null

    // Track how many times the correct answer dialog was shown for the current question
    private var correctAnswerDialogCount = 0

    // Track question start time for grading
    private var questionStartTime: Long = 0L
    private var fullyFailedQuestionCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NumberLineViewModel::class.java]
        CURRENT_POSITION = getString(R.string.current_position_label)

        tts = TTSUtility(requireContext()).apply {
            setSpeechRate(0.8f)
        }

        random = RandomValueGenerator()

        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener())
    }

    override fun onResume() {
        super.onResume()
        viewModel.reset()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.disableExploreByTouch(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameNumberLineBinding.inflate(inflater, container, false)
        setupObservers()
        setupUI()
        correctAnswerDesc = askNewQuestion(0)

        // Detect swipe on fragment root
        binding?.root?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        return binding!!.root
    }

    private fun setupObservers() {
        val updateNumberLineView = {
            val start = viewModel.lineStart.value ?: -5
            val end = viewModel.lineEnd.value ?: 5
            val position = viewModel.currentPosition.value ?: 0

            binding?.numberLineView?.updateNumberLine(start, end, position)
            binding?.currentPositionTv?.text = "$CURRENT_POSITION $position"

            // Cancel previous answer check runnable safely
            answerCheckRunnable?.let {
                handler.removeCallbacks(it)
            }

            // Schedule new answer check after 2 seconds
            answerCheckRunnable = Runnable {
                checkAnswer(start, end, position)
            }
            handler.postDelayed(answerCheckRunnable!!, 2000)
        }

        viewModel.lineStart.observe(viewLifecycleOwner) { updateNumberLineView() }
        viewModel.lineEnd.observe(viewLifecycleOwner) { updateNumberLineView() }
        viewModel.currentPosition.observe(viewLifecycleOwner) { updateNumberLineView() }
    }

    private fun checkAnswer(start: Int, end: Int, position: Int) {
        val questionKey = "$start-$end-$answer"
        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0 // seconds
        val totalTime = 15.0 // Set your question time limit here

        if (position == answer) {
            // Correct answer: reset counters and show dialog then new question
            wrongAttemptsForCurrentQuestion = 0
            lastQuestionKey = null
            correctAnswerDialogCount = 0

            val grade = GradingUtils.getGrade(elapsedTime, totalTime, true)
            context?.let { ctx ->
                activity?.layoutInflater?.let { inflater ->
                    DialogUtils.showResultDialog(ctx, inflater, tts!!, grade) {
                        correctAnswerDesc = askNewQuestion(answer)
                    }
                }
            }
        } else {
            // Wrong answer: process wrong attempts and possible dialogs
            onWrongAttempt(questionKey)
        }
    }

    private var totalWrongAttemptsForCurrentQuestion = 0

    private fun onWrongAttempt(questionKey: String) {
        if (lastQuestionKey == questionKey) {
            wrongAttemptsForCurrentQuestion++
            totalWrongAttemptsForCurrentQuestion++
        } else {
            lastQuestionKey = questionKey
            wrongAttemptsForCurrentQuestion = 1
            totalWrongAttemptsForCurrentQuestion = 1
            correctAnswerDialogCount = 0
        }

        if (wrongAttemptsForCurrentQuestion >= MAX_WRONG_ATTEMPTS) {
            correctAnswerDialogCount++

            context?.let { ctx ->
                activity?.layoutInflater?.let { inflater ->

                    val correctAnswerText = "The correct answer is $answer."

                    DialogUtils.showCorrectAnswerDialog(ctx, inflater, tts!!, correctAnswerText) {
                        when {
                            totalWrongAttemptsForCurrentQuestion < 6 -> {
                                // Allow retry same question
                                wrongAttemptsForCurrentQuestion = 0
                            }
                            else -> {
                                fullyFailedQuestionCount++

                                if (fullyFailedQuestionCount >= 3) {
                                    endGameWithScore()
                                    return@showCorrectAnswerDialog
                                }

                                // Otherwise move to new question
                                wrongAttemptsForCurrentQuestion = 0
                                totalWrongAttemptsForCurrentQuestion = 0
                                correctAnswerDialogCount = 0
                                lastQuestionKey = null
                                correctAnswerDesc = askNewQuestion(answer)
                            }
                        }

                    }
                }
            }

            return // ❗ Prevent retry dialog from showing
        }

        // Show retry dialog if not yet reached threshold
        val message = getString(R.string.wrong_answer)
        context?.let { ctx ->
            activity?.layoutInflater?.let { inflater ->
                DialogUtils.showRetryDialog(ctx, inflater, tts!!, message) {
                    // Retry dialog dismissed
                }
            }
        }
    }

    private fun setupUI() {
        binding?.numberLineQuestion?.setOnClickListener {
            tts?.speak(questionDesc)
        }

        binding?.btnLeft?.setOnClickListener {
            viewModel.moveLeft()
        }

        binding?.btnRight?.setOnClickListener {
            viewModel.moveRight()
        }
    }

    private fun askNewQuestion(position: Int): String {
        questionStartTime = System.currentTimeMillis()

        val topic = if (random!!.generateNumberLineQuestion()) Topic.ADDITION else Topic.SUBTRACTION
        val units = random!!.generateNumberForCountGame()
        val operator: String
        val direction: String

        answer = when (topic) {
            Topic.ADDITION -> {
                operator = getString(R.string.plus)
                direction = getString(R.string.right)
                position + units
            }
            Topic.SUBTRACTION -> {
                operator = getString(R.string.minus)
                direction = getString(R.string.left)
                position - units
            }
            else -> {
                operator = "?"
                direction = "?"
                position
            }
        }

        val questionBrief = getString(R.string.what_is, position.toString(), operator, units.toString())
        questionDesc = getString(R.string.standing_on, position.toString()) +
                getString(R.string.what_is, position.toString(), operator, units.toString()) +
                getString(R.string.units_to_direction, units.toString(), direction)

        binding?.numberLineQuestion?.text = questionBrief
        tts?.speak(questionDesc)

        return "$position $operator $units equals $answer"
    }


    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean = true

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null || e2 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                if (kotlin.math.abs(diffX) > SWIPE_THRESHOLD && kotlin.math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        Log.d(TAG, "Swipe Right detected in Fragment")
                        viewModel.moveRight()
                    } else {
                        Log.d(TAG, "Swipe Left detected in Fragment")
                        viewModel.moveLeft()
                    }
                    return true
                }
            }
            return false
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/numberline.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        super.onPause()
        // Cancel any pending answer checks on pause
        answerCheckRunnable?.let {
            handler.removeCallbacks(it)
        }
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.resetExploreByTouch(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
