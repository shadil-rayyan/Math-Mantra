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
import java.util.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("QuickPlayFragment", "onCreate called")
        setHasOptionsMenu(true)


        questionCategory = arguments?.getString(ARG_CATEGORY) ?: "default"
        Log.d("QuickPlayFragment", "Question category: $questionCategory")

        context?.let {
            ttsUtility = TTSUtility(it)
            val difficultyNum = DifficultyPreferences.getDifficulty(requireContext())  // returns Int, e.g. 1, 2, 3
            val difficulty = difficultyNum.toString()  // convert to "1", "2", "3"
            lang = LocaleHelper.getLanguage(it) ?: "en"

            Log.d("QuickPlayFragment", "Loading questions [lang=$lang, difficulty=$difficulty, category=$questionCategory]")

            questionList.clear()

            // Load questions asynchronously using lifecycleScope
            lifecycleScope.launch {
                try {
                    questionList.addAll(
                        ExcelQuestionLoader.loadQuestionsFromExcel(
                            context = it,
                            lang = lang,
                            mode = questionCategory ?: "default",
                            difficulty = difficulty
                        )
                    )
                    // Now that questions are loaded, check if it's successful
                    Log.d("QuickPlayFragment", "Loaded ${questionList.size} questions from Excel")
                    if (questionList.isNotEmpty()) {
                        loadNextQuestion()  // Now load the first question
                    } else {
                        Log.e("QuickPlayFragment", "No questions loaded!")
                        Toast.makeText(context, "No questions available.", Toast.LENGTH_SHORT).show()
                        endGame()  // Ends the game if no questions were loaded
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


        // Only attempt to load the next question if the list is not empty
        if (questionList.isNotEmpty()) {
            loadNextQuestion()
        } else {
            Log.d("QuickPlayFragment", "Question list is empty on view creation")
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true  // Ensure the hint item is visible
    }


    private fun loadNextQuestion() {
        // Ensure the list is loaded
        if (questionList.isEmpty()) {
            Log.e("QuickPlayFragment", "No questions available to load.")
            endGame()
            return
        }

        currentIndex++
        currentQuestionAttempts = 0

        if (currentIndex >= questionList.size) {
            Log.d("QuickPlayFragment", "No more questions. Ending game.")
            endGame()
            return
        }

        val gameQuestion = questionList[currentIndex]
        val questionText = gameQuestion.expression
        val correctAnswer = gameQuestion.answer
        currentQuestionTimeLimit = gameQuestion.timeLimit

        binding.questionTv.text = questionText
        val spokenQuestion = TTSHelper.formatMathText(questionText)
        // ttsUtility.speak(spokenQuestion)

        binding.answerEt.text?.clear()

        startTime = System.currentTimeMillis()
        totalQuestions = questionList.size
        Log.d("QuickPlayFragment", "Question ${currentIndex + 1}: $questionText (Answer: $correctAnswer)")
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) {
            Log.w("QuickPlayFragment", "checkAnswer called but currentIndex out of bounds")
            return
        }

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].answer
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0

        Log.d("QuickPlayFragment", "User input: $userInput, correct: $correctAnswer, elapsed: $elapsedSeconds sec")

        if (userInput == correctAnswer) {
            VibrationUtils.vibrate(requireContext(), 200)

            val grade = GradingUtils.getGrade(elapsedSeconds, currentQuestionTimeLimit.toDouble(), true)
            totalScore += GradingUtils.getPointsForGrade(grade)
            Log.d("QuickPlayFragment", "Correct! Grade: $grade, totalScore: $totalScore")

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
            Log.d("QuickPlayFragment", "Wrong attempt $currentQuestionAttempts / 3")

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
                Log.d("QuickPlayFragment", "Max attempts reached. Correct: $correctAnswer")

                val msg = "Wrong! The correct answer is $correctAnswer"
                DialogUtils.showRetryDialog(
                    requireContext(),
                    layoutInflater,
                    ttsUtility,
                    msg
                ) {
                    if (wrongQuestionsSet.size >= 7) {
                        Log.d("QuickPlayFragment", "Too many wrong answers. Ending game.")
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
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "quickplay")
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

        fun newInstance(category: String): QuickPlayFragment {
            val fragment = QuickPlayFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }
}
