package com.zendalona.zmantra.view.game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameTapBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.view.base.BaseGameFragment

class TapFragment : BaseGameFragment() {

    private var binding: FragmentGameTapBinding? = null
    private val handler = Handler(Looper.getMainLooper())

    private var questionIndex = 0
    private var tapCount = 0
    private var questions: List<GameQuestion> = emptyList()
    private var questionStartTime = 0L
    private var answerChecked = false
    private var isCheckingAnswer = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameTapBinding.inflate(inflater, container, false)

        binding?.root?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) onTap()
            true
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGifIfDefined()
    }

    override fun getModeName(): String = "tap"

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = questions
        startQuestion()
    }

    override fun getGifImageView() = binding?.animatedView
    override fun getGifResource(): Int? = R.drawable.game_angle_rotateyourphone

    private fun startQuestion() {
        if (questionIndex >= questions.size) {
            endGame()
            return
        }

        val question = questions[questionIndex]
        tapCount = 0
        attemptCount = 0
        answerChecked = false
        isCheckingAnswer = false
        questionStartTime = System.currentTimeMillis()

        val instruction = getString(R.string.tap_target_expression, question.expression)
        binding?.tapInstruction?.text = instruction
        binding?.tapInstruction?.contentDescription = instruction
        announce(binding?.tapInstruction, instruction)

        binding?.tapCount?.text = getString(R.string.initial_tap_count)
    }

    private fun onTap() {
        if (answerChecked || isCheckingAnswer || questionIndex >= questions.size) return

        tapCount++
        binding?.tapCount?.text = tapCount.toString()
        announce(binding?.tapCount, tapCount.toString())

        val question = questions[questionIndex]
        val correctAnswer = question.answer
        val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val rawLimit = question.timeLimit.toString().toDoubleOrNull()
        val timeLimit = if (rawLimit == null || rawLimit == 0.0) 10.0 else rawLimit

        if (tapCount == correctAnswer) {
            isCheckingAnswer = true
            handler.postDelayed({
                isCheckingAnswer = false
                answerChecked = true
                handleAnswerSubmission(
                    userAnswer = tapCount.toString(),
                    correctAnswer = correctAnswer.toString(),
                    elapsedTime = elapsedSeconds,
                    timeLimit = timeLimit,
                    onCorrect = { goToNextQuestion() },
                    onIncorrect = {},
                    onShowCorrect = { goToNextQuestion() }
                )
            }, 3000)

        } else if (tapCount > correctAnswer) {
            attemptCount++
            if (attemptCount >= 3) {
                answerChecked = true
                handleAnswerSubmission(
                    userAnswer = "wrong",
                    correctAnswer = correctAnswer.toString(),
                    elapsedTime = elapsedSeconds,
                    timeLimit = timeLimit,
                    onCorrect = {},
                    onIncorrect = {},
                    onShowCorrect = {
                        goToNextQuestion()
                    }
                )
            } else {
                // retry allowed, reset counter and prompt again
                tapCount = 0
                binding?.tapCount?.text = getString(R.string.initial_tap_count)
                announce(binding?.tapCount, binding?.tapCount?.text.toString())
            }
        }
    }

    private fun goToNextQuestion() {
        questionIndex++
        handler.postDelayed({ startQuestion() }, 1200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        binding = null
    }
}
