package com.zendalona.mathsmantra.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentQuickPlayBinding
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.GradingUtils
import com.zendalona.mathsmantra.utility.common.TTSHelper
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.common.TTSUtility
import java.io.IOException
import java.util.*

class QuickPlayFragment : Fragment() {

    private var _binding: FragmentQuickPlayBinding? = null
    private val binding get() = _binding!!

    private var rawQuestions = mutableListOf<String>()
    private val questionList = mutableListOf<Pair<String, Int>>()
    private val wrongQuestionsSet = mutableSetOf<Int>()

    private var currentIndex = -1
    private var currentQuestionAttempts = 0
    private var totalScore = 0
    private var totalQuestions = 0
    private lateinit var difficulty: String
    private var startTime: Long = 0
    private var currentQuestionTimeLimit = 60
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var ttsUtility: TTSUtility

    private var questionCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get category from arguments or default to "default"
        questionCategory = arguments?.getString(ARG_CATEGORY) ?: "default"

        ttsUtility = TTSUtility(requireContext())
        difficulty = DifficultyPreferences.getDifficulty(requireContext())

        loadRawQuestionsFromAssets(questionCategory!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickPlayBinding.inflate(inflater, container, false)
        binding.submitAnswerBtn.setOnClickListener { checkAnswer() }
        loadNextQuestion()
        return binding.root
    }

    private fun loadRawQuestionsFromAssets(category: String) {
        val filename = "quickplay/$category/quickplay_${difficulty.lowercase(Locale.ROOT)}.txt"
        try {
            val lines = requireContext().assets.open(filename).bufferedReader().readLines()
            rawQuestions.clear()
            for (line in lines) {
                // Each line must have at least 3 parts separated by "==="
                if (line.isNotBlank() && line.split("===").size >= 3) {
                    rawQuestions.add(line.trim())
                }
            }
            if (rawQuestions.isEmpty()) {
                Toast.makeText(requireContext(), "No valid questions found", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error reading file: $filename", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadNextQuestion() {
        currentIndex++
        currentQuestionAttempts = 0

        if (currentIndex >= rawQuestions.size) {
            endGame()
            return
        }

        val parts = rawQuestions[currentIndex].split("===")
        val rawExpression = parts[0].trim()
        val timeLimit = parts[1].trim().toIntOrNull() ?: 20

        // Use your existing parser
        val (questionText, correctAnswer) = QuestionParser.parseExpression(rawExpression)

        if (questionList.size > currentIndex) {
            questionList[currentIndex] = questionText to correctAnswer
        } else {
            questionList.add(questionText to correctAnswer)
        }

        binding.questionTv.text = questionText

        val spokenQuestion = TTSHelper.formatMathText(questionText)
        ttsUtility.speak(spokenQuestion)

        binding.answerEt.text?.clear()
        startTime = System.currentTimeMillis()
        currentQuestionTimeLimit = timeLimit
        totalQuestions = rawQuestions.size
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) return

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].second
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0

        if (userInput == correctAnswer) {
            val grade = GradingUtils.getGrade(elapsedSeconds, currentQuestionTimeLimit.toDouble(), true)
            totalScore += GradingUtils.getPointsForGrade(grade)
            playSound("correct")

            DialogUtils.showResultDialog(
                requireContext(),
                layoutInflater,
                ttsUtility,
                grade
            ) {
                loadNextQuestion()
            }

        } else {
            playSound("wrong")
            currentQuestionAttempts++
            if (currentQuestionAttempts < 3) {
                DialogUtils.showRetryDialog(
                    requireContext(),
                    layoutInflater,
                    ttsUtility,
                    "Wrong! Try again. Attempt $currentQuestionAttempts of 3."
                ) {
                    binding.answerEt.text?.clear()
                }
            } else {
                totalScore += GradingUtils.getPointsForGrade("Wrong Answer")
                wrongQuestionsSet.add(currentIndex)
                val msg = "Wrong! The correct answer is $correctAnswer"
                DialogUtils.showRetryDialog(
                    requireContext(),
                    layoutInflater,
                    ttsUtility,
                    msg
                ) {
                    if (wrongQuestionsSet.size >= 4) {
                        endGame()
                    } else {
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    private fun endGame() {
        val maxPossibleScore = 50 * totalQuestions
        val percentage = if (maxPossibleScore > 0) {
            (totalScore.toDouble() * 100) / maxPossibleScore
        } else 0.0

        Toast.makeText(requireContext(), "Quiz Over! Score: $totalScore", Toast.LENGTH_LONG).show()

        val spokenEnd = TTSHelper.formatMathText("Quiz over! Your final score is $totalScore")
        ttsUtility.speak(spokenEnd)

        val bundle = Bundle().apply {
            putInt("score", totalScore)
            putInt("totalQuestions", totalQuestions)
            putDouble("percentage", percentage)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EndScoreFragment::class.java, bundle)
            .commit()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
        ttsUtility.shutdown()
    }

    companion object {
        private const val ARG_CATEGORY = "question_category"

        fun newInstance(category: String): QuickPlayFragment {
            val fragment = QuickPlayFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }
}
