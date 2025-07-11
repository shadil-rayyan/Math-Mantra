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

class MentalCalculationFragment : Fragment(), Hintable {

    private var binding: FragmentGameMentalCalculationBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tts: TTSUtility

    private var questionList: List<GameQuestion> = emptyList()
    private var currentQuestionIndex = 0
    private var correctAnswer = 0
    private var wrongAttempts = 0
    private var startTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameMentalCalculationBinding.inflate(inflater, container, false)
        tts = TTSUtility(requireContext())

        val lang = LocaleHelper.getLanguage(requireContext())
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())

        lifecycleScope.launch {
            questionList = ExcelQuestionLoader.loadQuestionsFromExcel(
                requireContext(),
                lang,
                "mental",
                difficulty.toString()
            )
        }
        if (questionList.isEmpty()) {
            questionList = listOf(GameQuestion("1 + 2", 3))
        }

        loadNextQuestion()

        binding?.submitAnswerBtn?.setOnClickListener { checkAnswer() }
        binding?.answerEt?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else false
        }

        setHasOptionsMenu(true)
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun loadNextQuestion() {
        if (currentQuestionIndex >= questionList.size) {
            if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                tts.speak(getString(R.string.shake_game_over))
            }
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

        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            val spokenText = TTSHelper.formatMathText(question.expression)
            handler.postDelayed({
                tts.speak("Solve $spokenText")
            }, 1000)
        }

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
            contentDescription = null
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_NONE
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
                if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                    tts.speak("Correct")
                }
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
                    if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                        tts.speak(getString(R.string.shake_game_over))
                    }
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
            putString("mode", "mental")
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
