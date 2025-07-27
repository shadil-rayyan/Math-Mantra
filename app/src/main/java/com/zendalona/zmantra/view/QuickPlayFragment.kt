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
import kotlinx.coroutines.launch

class QuickPlayFragment : Fragment(), Hintable {

    private var _binding: FragmentQuickPlayBinding? = null
    private val binding get() = _binding!!

    private val questionList = mutableListOf<GameQuestion>()
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
    private var hintMode: String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        questionCategory = arguments?.getString(ARG_CATEGORY) ?: "default"
        hintMode = arguments?.getString(ARG_HINT_MODE) ?: "default"
        Log.d("QuickPlayFragment", "Category: $questionCategory, HintMode: $hintMode")

        context?.let {
            ttsUtility = TTSUtility(it)
            val difficultyNum = DifficultyPreferences.getDifficulty(requireContext())
            val difficulty = difficultyNum.toString()
            lang = LocaleHelper.getLanguage(it) ?: "en"

            lifecycleScope.launch {
                try {
                    questionList.clear()
                    questionList.addAll(
                        ExcelQuestionLoader.loadQuestionsFromExcel(
                            context = it,
                            lang = lang,
                            mode = questionCategory ?: "default",
                            difficulty = difficulty
                        )
                    )

                    if (questionList.isNotEmpty()) {
                        loadNextQuestion()
                    } else {
                        Toast.makeText(context, "No questions available.", Toast.LENGTH_SHORT).show()
                        endGame()
                    }

                } catch (e: Exception) {
                    Log.e("QuickPlayFragment", "Error loading questions: ${e.message}")
                    Toast.makeText(context, "Error loading questions", Toast.LENGTH_SHORT).show()
                    endGame()
                }
            }
        } ?: run {
            difficulty = "easy"
            lang = "en"
            Log.w("QuickPlayFragment", "Context null, fallback to default difficulty/lang")
        }
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

        if (questionList.isNotEmpty()) {
            loadNextQuestion()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    fun loadNextQuestion() {
        if (questionList.isEmpty()) {
            endGame()
            return
        }

        currentIndex++
        currentQuestionAttempts = 0

        if (currentIndex >= questionList.size) {
            endGame()
            return
        }

        val gameQuestion = questionList[currentIndex]
        val questionText = gameQuestion.expression
        val correctAnswer = gameQuestion.answer
        currentQuestionTimeLimit = gameQuestion.timeLimit

        binding.questionTv.text = questionText
        binding.answerEt.text?.clear()

        startTime = System.currentTimeMillis()
        totalQuestions = questionList.size
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) return

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].answer
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0

        if (userInput == correctAnswer) {
            VibrationUtils.vibrate(requireContext(), 200)

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
            VibrationUtils.vibrate(requireContext(), 400)
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
                    if (wrongQuestionsSet.size >= 7) {
                        endGame()
                    } else {
                        loadNextQuestion()
                    }
                }
            }
        }
    }

    fun endGame() {
        val spokenEnd = TTSHelper.formatMathText("Quiz over! Your final score is $totalScore")
        ttsUtility.speak(spokenEnd)
        endGameWithScore()
    }

    fun playSound(name: String) {
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

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", hintMode)
        }
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
        ttsUtility.shutdown()
    }

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
