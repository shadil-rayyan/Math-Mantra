package com.zendalona.zmantra.view.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameMentalCalculationBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSHelper
import com.zendalona.zmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import kotlinx.coroutines.launch
import com.zendalona.zmantra.utility.common.*

class MentalCalculationFragment : Fragment(), Hintable {

    private var binding: FragmentGameMentalCalculationBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tts: TTSUtility

    private var questionList: List<GameQuestion> = emptyList()
    private var currentQuestionIndex = 0
    private var correctAnswer = 0
    private var wrongAttempts = 0
    private var revealIndex = 0
    private var revealTokens: List<String> = listOf()
    private var isRevealing = false
    private var startTime: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameMentalCalculationBinding.inflate(inflater, container, false)
        tts = TTSUtility(requireContext())

        lifecycleScope.launch {
            questionList = ExcelQuestionLoader.loadQuestionsFromExcel(
                requireContext(),
                LocaleHelper.getLanguage(requireContext()),
                "mental",
                DifficultyPreferences.getDifficulty(requireContext()).toString()
            )
            if (questionList.isEmpty()) questionList = listOf(GameQuestion("1 + 2", 3))
            loadNextQuestion()
        }

        binding?.apply {
            readQuestionBtn.setOnClickListener { onReadQuestionClicked() }
            submitAnswerBtn.setOnClickListener { checkAnswer() }
            answerEt.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkAnswer()
                    true
                } else false
            }
        }

        setHasOptionsMenu(true)
        return binding!!.root
    }

    private fun onReadQuestionClicked() {
        // Interrupt any ongoing reveal
        if (isRevealing) handler.removeCallbacksAndMessages(null)
        val question = questionList.getOrNull(currentQuestionIndex) ?: return
        val expression = question.expression

        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            // TalkBack users: announce full question
            binding?.mentalCalculation?.announceForAccessibility(expression)
        } else {
            // Visual reveal for non-TalkBack users
            revealTokens = expression.split(" ")
            revealIndex = 0
            isRevealing = true
            revealNextToken()
        }
    }

    private fun revealNextToken() {
        if (revealIndex >= revealTokens.size) {
            isRevealing = false
            return
        }
        val token = revealTokens[revealIndex].replace("/", "÷")
        binding?.mentalCalculation?.apply {
            text = token
            contentDescription = null
        }
        revealIndex++
        handler.postDelayed({
            binding?.mentalCalculation?.text = ""
            revealNextToken()
        }, 1000)
    }

    private fun loadNextQuestion() {
        if (currentQuestionIndex >= questionList.size) {
            if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                tts.speak(getString(R.string.shake_game_over))
            }
            return
        }
        wrongAttempts = 0
        val q = questionList[currentQuestionIndex]
        correctAnswer = q.answer
        binding?.apply {
            answerEt.setText("")
            mentalCalculation.text = ""
            answerEt.isEnabled = false
            submitAnswerBtn.isEnabled = false
        }

        val tokens = q.expression.split(" ")
        startTime = System.currentTimeMillis()

        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            handler.postDelayed({
                tts.speak("Solve ${TTSHelper.formatMathText(q.expression)}")
            }, 500)
        }

        handler.postDelayed({
            binding?.answerEt?.isEnabled = true
            binding?.submitAnswerBtn?.isEnabled = true
            binding?.answerEt?.requestFocus()
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
        val isCorrect = userAnswer == correctAnswer
        val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
        val grade = GradingUtils.getGrade(elapsedSec, questionList[currentQuestionIndex].timeLimit.toDouble(), isCorrect)

        if (isCorrect) {
            if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) tts.speak(getString(R.string.correct_answer))
            if (questionList[currentQuestionIndex].celebration) MediaPlayer.create(context, R.raw.bell_ring)?.start()
            DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                currentQuestionIndex++; loadNextQuestion()
            }
        } else {
            wrongAttempts++
            if (wrongAttempts >= 3) {
                if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) tts.speak(getString(R.string.shake_game_over))

            } else {
                DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.shake_failure)) {
                    binding?.answerEt?.setText("")
                    binding?.answerEt?.requestFocus()
                }
            }
        }
    }

    override fun showHint() {
        val hintFragment = HintFragment().apply {
            arguments = Bundle().apply { putString("mode", "mental") }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
        binding = null
    }
}
