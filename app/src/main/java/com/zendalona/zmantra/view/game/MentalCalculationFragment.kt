package com.zendalona.zmantra.view.game

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameMentalCalculationBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.utility.common.TTSHelper
import com.zendalona.zmantra.view.base.BaseGameFragment

class MentalCalculationFragment : BaseGameFragment() {

    private var binding: FragmentGameMentalCalculationBinding? = null
    private val handler = Handler(Looper.getMainLooper())

    private var questions: List<GameQuestion> = emptyList()
    private var currentIndex = 0
    private var correctAnswer: Int = 0
    private var startTime: Long = 0

    private var revealTokens: List<String> = listOf()
    private var revealIndex = 0
    private var isRevealing = false

    override fun getModeName(): String = "mental"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameMentalCalculationBinding.inflate(inflater, container, false)

        binding?.apply {
            readQuestionBtn.setOnClickListener { onReadQuestionClicked() }
            submitAnswerBtn.setOnClickListener { checkAnswer() }
            answerEt.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    checkAnswer()
                    true
                } else false
            }
        }

        return binding!!.root
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = if (questions.isEmpty()) listOf(GameQuestion("1 + 2", 3)) else questions
        loadNextQuestion()
    }

    private fun onReadQuestionClicked() {
        if (isRevealing) handler.removeCallbacksAndMessages(null)

        val question = questions.getOrNull(currentIndex) ?: return
        revealTokens = question.expression.split(" ")
        revealIndex = 0
        isRevealing = true
        revealNextToken()
    }

    private fun revealNextToken() {
        if (revealIndex >= revealTokens.size) {
            isRevealing = false
            return
        }

        val token = revealTokens[revealIndex].replace("/", "÷")
        binding?.mentalCalculation?.apply {
            text = token
            if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                announceForAccessibility(token)
            }
        }

        revealIndex++

        handler.postDelayed({
            binding?.mentalCalculation?.text = ""
            revealNextToken()
        }, 1000)
    }

    private fun loadNextQuestion() {
        if (currentIndex >= questions.size) {
            endGame()
            return
        }

        attemptCount = 0
        val question = questions[currentIndex]
        correctAnswer = question.answer

        binding?.apply {
            answerEt.setText("")
            mentalCalculation.text = ""
            answerEt.isEnabled = false
            submitAnswerBtn.isEnabled = false
        }

        val tokens = question.expression.split(" ")
        startTime = System.currentTimeMillis()

        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            handler.postDelayed({
                tts.speak("Solve ${TTSHelper.formatMathText(question.expression)}")
            }, 500)
        }

        handler.postDelayed({
            binding?.apply {
                answerEt.isEnabled = true
                submitAnswerBtn.isEnabled = true
                answerEt.requestFocus()
            }
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding?.answerEt, InputMethodManager.SHOW_IMPLICIT)
        }, tokens.size * 1000L + 500)
    }

    private fun checkAnswer() {
        val input = binding?.answerEt?.text.toString().trim()
        if (input.isEmpty()) {
            Toast.makeText(context, R.string.enter_answer, Toast.LENGTH_SHORT).show()
            return
        }

        val userAnswer = input.toIntOrNull()
        if (userAnswer == null) {
            Toast.makeText(context, R.string.wrong_answer, Toast.LENGTH_SHORT).show()
            return
        }

        val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
        val timeLimit = questions[currentIndex].timeLimit.toDouble()

        handleAnswerSubmission(
            userAnswer = userAnswer.toString(),
            correctAnswer = correctAnswer.toString(),
            elapsedTime = elapsedSec,
            timeLimit = timeLimit,
            onCorrect = {
                if (questions[currentIndex].celebration) {
                    MediaPlayer.create(context, R.raw.bell_ring)?.start()
                }
                currentIndex++
                loadNextQuestion()
            },
            onIncorrect = {
                binding?.answerEt?.setText("")
                binding?.answerEt?.requestFocus()
            },
            onShowCorrect = {
                currentIndex++
                loadNextQuestion()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
