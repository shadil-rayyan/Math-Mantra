package com.zendalona.mathsmantra.ui

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentQuickPlayBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.common.ScorePageFragment
import com.zendalona.mathsmantra.utility.common.GradingUtils
import com.zendalona.mathsmantra.utility.common.TTSHelper
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.utility.common.VibrationUtils
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import com.zendalona.mathsmantra.utility.story.StoryQuestionGenerator
import java.io.IOException
import java.util.*

class QuickPlayFragment : Fragment(), Hintable {

    private var _binding: FragmentQuickPlayBinding? = null
    private val binding get() = _binding!!

    private val rawQuestions = mutableListOf<String>()
    private val questionList = mutableListOf<Pair<String, Int>>()
    private val wrongQuestionsSet = mutableSetOf<Int>()

    private var currentIndex = -1
    private var currentQuestionAttempts = 0
    private var totalScore = 0
    private var totalQuestions = 0

    private lateinit var difficulty: String
    private lateinit var lang: String
    private var startTime: Long = 0
    private var currentQuestionTimeLimit = 60

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var ttsUtility: TTSUtility

    private var questionCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("QuickPlayFragment", "onCreate called")

        questionCategory = arguments?.getString(ARG_CATEGORY) ?: "default"
        Log.d("QuickPlayFragment", "Question category: $questionCategory")

        context?.let {
            ttsUtility = TTSUtility(it)
            difficulty = DifficultyPreferences.getDifficulty(it)
            lang = LocaleHelper.getLanguage(it) ?: "en"
            Log.d("QuickPlayFragment", "Initialized ttsUtility, difficulty: $difficulty, language: $lang")
            loadRawQuestionsFromAssets(questionCategory!!)
        } ?: run {
            difficulty = "easy"
            lang = "en"
            Log.w("QuickPlayFragment", "Context is null during onCreate, defaulting difficulty and lang")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("QuickPlayFragment", "onCreateView called")
        _binding = FragmentQuickPlayBinding.inflate(inflater, container, false)

        binding.submitAnswerBtn.setOnClickListener {
            Log.d("QuickPlayFragment", "Submit Answer button clicked")
            checkAnswer()
        }

        loadNextQuestion()

        return binding.root
    }

    private fun loadRawQuestionsFromAssets(category: String) {
        // Example file path: "numbers/landingpage/quickplay/easy.txt"
        val filename = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
        Log.d("QuickPlayFragment", "Loading questions from file: $filename")

        try {
            val lines = requireContext().assets.open(filename).bufferedReader().readLines()
            rawQuestions.clear()
            for (line in lines) {
                if (line.isNotBlank() && line.split("===").size >= 3) {
                    rawQuestions.add(line.trim())
                } else {
                    Log.d("QuickPlayFragment", "Skipping invalid question line: $line")
                }
            }
            Log.d("QuickPlayFragment", "Loaded ${rawQuestions.size} valid questions")

            if (rawQuestions.isEmpty()) {
                Toast.makeText(requireContext(), "No valid questions found", Toast.LENGTH_LONG).show()
                Log.w("QuickPlayFragment", "No valid questions found in asset file")
            }
        } catch (e: IOException) {
            Log.e("QuickPlayFragment", "IOException reading file: $filename", e)
            Toast.makeText(requireContext(), "Error reading file: $filename", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadNextQuestion() {
        currentIndex++
        currentQuestionAttempts = 0

        if (currentIndex >= rawQuestions.size) {
            Log.d("QuickPlayFragment", "No more questions. Ending game.")
            endGame()
            return
        }

        val rawLine = rawQuestions[currentIndex]
        Log.d("QuickPlayFragment", "Loading question $currentIndex: rawLine=$rawLine")

        val parts = rawLine.split("===")
        if (parts.size < 3) {
            Log.w("QuickPlayFragment", "Invalid question format at index $currentIndex")
            loadNextQuestion()
            return
        }

        val metadata = parts[0].trim()   // e.g. "add:4+3"
        val timeLimit = parts[1].trim().toIntOrNull() ?: 20

        // Generate story question text and get correct answer
        val (questionText, correctAnswer) = StoryQuestionGenerator.generateStoryQuestion(requireContext(), metadata)

        Log.d("QuickPlayFragment", "Generated story question: $questionText, correctAnswer: $correctAnswer")

        if (questionList.size > currentIndex) {
            questionList[currentIndex] = questionText to correctAnswer
        } else {
            questionList.add(questionText to correctAnswer)
        }

        binding.questionTv.text = questionText
        val spokenQuestion = TTSHelper.formatMathText(questionText)
        ttsUtility.speak(spokenQuestion)
        Log.d("QuickPlayFragment", "Spoke question")

        binding.answerEt.text?.clear()

        startTime = System.currentTimeMillis()
        currentQuestionTimeLimit = timeLimit
        totalQuestions = rawQuestions.size
        Log.d("QuickPlayFragment", "Question timer started. Time limit: $currentQuestionTimeLimit seconds")
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) {
            Log.w("QuickPlayFragment", "checkAnswer called but currentIndex out of bounds")
            return
        }

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].second
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0

        Log.d("QuickPlayFragment", "User input: $userInput, correct answer: $correctAnswer, elapsed time: $elapsedSeconds sec")

        if (userInput == correctAnswer) {
            VibrationUtils.vibrate(requireContext(), 200) // short

            val grade = GradingUtils.getGrade(elapsedSeconds, currentQuestionTimeLimit.toDouble(), true)
            totalScore += GradingUtils.getPointsForGrade(grade)
            Log.d("QuickPlayFragment", "Correct answer! Grade: $grade, totalScore: $totalScore")

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
            VibrationUtils.vibrate(requireContext(), 400)
            currentQuestionAttempts++
            Log.d("QuickPlayFragment", "Wrong answer attempt $currentQuestionAttempts of 3")

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
                Log.d("QuickPlayFragment", "Max attempts reached for question $currentIndex. Correct answer: $correctAnswer")

                val msg = "Wrong! The correct answer is $correctAnswer"
                DialogUtils.showRetryDialog(
                    requireContext(),
                    layoutInflater,
                    ttsUtility,
                    msg
                ) {
                    if (wrongQuestionsSet.size >= 7) {
                        Log.d("QuickPlayFragment", "Wrong questions limit reached (${wrongQuestionsSet.size}). Ending game.")
                        endGame()
                    } else {
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    private fun endGame() {
        val spokenEnd = TTSHelper.formatMathText("Quiz over! Your final score is $totalScore")
        ttsUtility.speak(spokenEnd)

        endGameWithScore()
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
            Log.d("QuickPlayFragment", "Playing sound: $name")
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/quickplay.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("QuickPlayFragment", "onDestroyView called - releasing mediaPlayer and shutting down TTS")
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
