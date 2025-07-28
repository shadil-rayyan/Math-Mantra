package com.zendalona.zmantra.view

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentQuickPlayBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSHelper
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.common.VibrationUtils
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.view.base.BaseGameFragment
import kotlinx.coroutines.launch

class QuickPlayFragment : BaseGameFragment() {

    private var _binding: FragmentQuickPlayBinding? = null
    private val binding get() = _binding!!

    private val questionList = mutableListOf<GameQuestion>()
    private val wrongQuestionsSet = mutableSetOf<Int>()

    private var currentIndex = -1
    private var currentQuestionAttempts = 0
    private var totalScore = 0
    private var totalQuestions = 0

    private var startTime: Long = 0
    private var currentQuestionTimeLimit = 60

    private var mediaPlayer: MediaPlayer? = null
    private var questionCategory: String? = null
    private var hintMode: String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionCategory = arguments?.getString(ARG_CATEGORY) ?: "default"
        hintMode = arguments?.getString(ARG_HINT_MODE) ?: "default"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickPlayBinding.inflate(inflater, container, false)

        binding.submitAnswerBtn.setOnClickListener {
            checkAnswer()
        }

        return binding.root
    }

    override fun getModeName(): String = questionCategory ?: "default"

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        questionList.clear()
        questionList.addAll(questions)
        loadNextQuestion()
    }

    private fun loadNextQuestion() {
        if (questionList.isEmpty() || currentIndex + 1 >= questionList.size) {
            endGame()
            return
        }

        currentIndex++
        currentQuestionAttempts = 0

        val gameQuestion = questionList[currentIndex]
        currentQuestionTimeLimit = gameQuestion.timeLimit
        startTime = System.currentTimeMillis()

        binding.questionTv.text = gameQuestion.expression
        binding.answerEt.text?.clear()
        totalQuestions = questionList.size
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) return

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].answer
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0

        if (userInput == correctAnswer) {
            VibrationUtils.vibrate(requireContext(), 200)
            val grade = getGrade(elapsedSeconds, currentQuestionTimeLimit.toDouble())
            totalScore += GradingUtils.getPointsForGrade(grade)
            playSound("correct")
            showResultDialog(grade) { loadNextQuestion() }

        } else {
            playSound("wrong")
            VibrationUtils.vibrate(requireContext(), 400)
            currentQuestionAttempts++

            if (currentQuestionAttempts < 3) {
                DialogUtils.showRetryDialog(
                    requireContext(), layoutInflater, tts,
                    "Wrong! Try again. Attempt $currentQuestionAttempts of 3."
                ) {
                    binding.answerEt.text?.clear()
                }
            } else {
                totalScore += GradingUtils.getPointsForGrade("Wrong Answer")
                wrongQuestionsSet.add(currentIndex)

                val msg = "Wrong! The correct answer is $correctAnswer"
                DialogUtils.showRetryDialog(
                    requireContext(), layoutInflater, tts, msg
                ) {
                    if (wrongQuestionsSet.size >= 7) endGame()
                    else loadNextQuestion()
                }
            }
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply { putString("mode", hintMode) }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun playSound(name: String) {
        val resId = when (name) {
            "correct" -> R.raw.correct_sound
            "wrong" -> R.raw.wrong_sound
            else -> null
        }
        resId?.let {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(requireContext(), it)
            mediaPlayer?.start()
        }
    }

//    private fun endGame() {
//        val spokenEnd = com.zendalona.zmantra.utility.common.TTSHelper.formatMathText("Quiz over! Your final score is $totalScore")
//        tts.speak(spokenEnd)
//        endGameWithScore()
//    }

    companion object {
        private const val ARG_CATEGORY = "question_category"
        private const val ARG_HINT_MODE = "hint_mode"

        fun newInstance(category: String, hintMode: String): QuickPlayFragment {
            val fragment = QuickPlayFragment()
            val args = Bundle().apply {
                putString(ARG_CATEGORY, category)
                putString(ARG_HINT_MODE, hintMode)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
