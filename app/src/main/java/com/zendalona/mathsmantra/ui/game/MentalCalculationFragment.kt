// Updated MentalCalculationFragment.kt
package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameMentalCalculationBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.common.GradingUtils
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class MentalCalculationFragment : Fragment(), Hintable {

    private var binding: FragmentGameMentalCalculationBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tts: TTSUtility

    private var questionList: List<MentalQuestion> = emptyList()
    private var currentQuestionIndex = 0
    private var correctAnswer = 0
    private var wrongAttempts = 0
    private var startTime: Long = 0

    private data class MentalQuestion(
        val expression: String,
        val answer: Int,
        val celebration: Boolean = false,
        val timeLimit: Int = 30
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameMentalCalculationBinding.inflate(inflater, container, false)
        tts = TTSUtility(requireContext())

        val lang = LocaleHelper.getLanguage(requireContext())
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())
        questionList = loadQuestionsFromAssets(lang, difficulty)

        if (questionList.isEmpty()) {
            questionList = listOf(MentalQuestion("1 + 2", 3))
        }

        loadNextQuestion()

        binding?.submitAnswerBtn?.setOnClickListener { checkAnswer() }
        binding?.answerEt?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else false
        }

        return binding!!.root
    }

    private fun loadQuestionsFromAssets(lang: String, difficulty: String): List<MentalQuestion> {
        val list = mutableListOf<MentalQuestion>()
        val fileName = "$lang/game/mentalcalculation/${difficulty.lowercase(Locale.ROOT)}.txt"
        try {
            val reader = BufferedReader(InputStreamReader(requireContext().assets.open(fileName)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    val parts = it.split("===")
                    if (parts.isNotEmpty()) {
                        val (expression, answer) = QuestionParser.parseExpression(parts[0])
                        val timeLimit = parts.getOrNull(1)?.toIntOrNull() ?: 20
                        val celebration = parts.getOrNull(2)?.toIntOrNull() == 1
                        list.add(MentalQuestion(expression, answer, celebration, timeLimit))
                    }
                }
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Error loading questions: $fileName", Toast.LENGTH_SHORT).show()
        }
        return list
    }

    private fun loadNextQuestion() {
        if (currentQuestionIndex >= questionList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
            return
        }

        wrongAttempts = 0
        val question = questionList[currentQuestionIndex]
        correctAnswer = question.answer
        binding?.answerEt?.setText("")
        binding?.mentalCalculation?.text = ""
        binding?.answerEt?.isEnabled = false
        binding?.submitAnswerBtn?.isEnabled = false

        val tokens = question.expression.split(" ")
        startTime = System.currentTimeMillis()

//        tts.speak("Solve " + question.expression.replace("+", " plus").replace("-", " minus").replace("*", " times").replace("/", " divided by"))
        revealTokens(tokens, 0)
    }

    private fun revealTokens(tokens: List<String>, index: Int) {
        if (index >= tokens.size) {
            handler.postDelayed({
                binding?.answerEt?.isEnabled = true
                binding?.submitAnswerBtn?.isEnabled = true
                binding?.answerEt?.requestFocus()
            }, 300)
            return
        }

        val token = tokens[index].replace("/", "÷")
        binding?.mentalCalculation?.apply {
            text = token
            contentDescription = token
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            isFocusable = true
            isFocusableInTouchMode = true
            postDelayed({ requestFocus(); announceForAccessibility(token) }, 200)
        }

        handler.postDelayed({
            binding?.mentalCalculation?.text = ""
            revealTokens(tokens, index + 1)
        }, 4000)
    }

    private fun checkAnswer() {
        val userInput = binding?.answerEt?.text.toString().trim()
        if (userInput.isEmpty()) {
            Toast.makeText(context, "Enter your answer", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val userAnswer = userInput.toInt()
            val isCorrect = userAnswer == correctAnswer
            val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
            val question = questionList[currentQuestionIndex]
            val grade = GradingUtils.getGrade(elapsedSeconds, question.timeLimit.toDouble(), isCorrect)

            if (isCorrect) {
                tts.speak("Correct")
                if (question.celebration) {
                    MediaPlayer.create(context, R.raw.bell_ring)?.start()
                }
                DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                    currentQuestionIndex++
                    loadNextQuestion()
                }
            } else {
                wrongAttempts++
                if (wrongAttempts >= 3) {
//                    tts.speak(getString(R.string.shake_game_over))
                    endGameWithScore()
                } else {
                    DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.shake_failure)) {
                        binding?.answerEt?.setText("")
                        binding?.answerEt?.requestFocus()
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/mentalcalculation.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

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
