package com.zendalona.zmantra.view.game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameNumberLineBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.viewModel.NumberLineViewModel
import kotlinx.coroutines.launch
import java.util.*

private val handler = Handler(Looper.getMainLooper())

class NumberLineFragment : Fragment(), Hintable {

    companion object {
        private const val TAG = "NumberLineFragment"
        private const val MAX_WRONG_ATTEMPTS = 3
    }

    private var binding: FragmentGameNumberLineBinding? = null
    private lateinit var viewModel: NumberLineViewModel
    private var tts: TTSUtility? = null

    private var answer = 0
    private var questionDesc = ""
    private var correctAnswerDesc = ""
    private var questionStartTime: Long = 0L

    private var answerCheckRunnable: Runnable? = null

    private var lastQuestionKey: String? = null
    private var wrongAttemptsForCurrentQuestion = 0
    private var correctAnswerDialogCount = 0
    private var totalWrongAttemptsForCurrentQuestion = 0
    private var fullyFailedQuestionCount = 0

    private var CURRENT_POSITION: String? = null

    // ✅ Excel-based questions
    private var excelQuestions: List<GameQuestion> = emptyList()
    private var currentExcelQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NumberLineViewModel::class.java]
        CURRENT_POSITION = getString(R.string.current_position_label)

        tts = TTSUtility(requireContext()).apply { setSpeechRate(0.8f) }
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())
        val lang = LocaleHelper.getLanguage(context) ?: "en"
        // ✅ Load Excel questions for mode = numberline, difficulty = 1
        lifecycleScope.launch {

            excelQuestions = ExcelQuestionLoader.loadQuestionsFromExcel(
                requireContext(),
                lang = lang,
                mode = "numberline",
                difficulty = difficulty.toString()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameNumberLineBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        setupObservers()
        setupUI()
        correctAnswerDesc = askNewQuestion()

        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun setupObservers() {
        val updateNumberLineView = {
            val start = viewModel.lineStart.value ?: -5
            val end = viewModel.lineEnd.value ?: 5
            val position = viewModel.currentPosition.value ?: 0

            binding?.numberLineView?.updateNumberLine(start, end, position)
            binding?.currentPositionTv?.text = "$CURRENT_POSITION $position"

            // Cancel previous
            answerCheckRunnable?.let { handler.removeCallbacks(it) }

            answerCheckRunnable = Runnable { checkAnswer(start, end, position) }
            handler.postDelayed(answerCheckRunnable!!, 2000)
        }

        viewModel.lineStart.observe(viewLifecycleOwner) { updateNumberLineView() }
        viewModel.lineEnd.observe(viewLifecycleOwner) { updateNumberLineView() }
        viewModel.currentPosition.observe(viewLifecycleOwner) { updateNumberLineView() }
    }

    private fun checkAnswer(start: Int, end: Int, position: Int) {
        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val totalTime = 15.0

        if (position == answer) {
            val grade = GradingUtils.getGrade(elapsedTime, totalTime, true)
            context?.let { ctx ->
                activity?.layoutInflater?.let { inflater ->
                    DialogUtils.showResultDialog(ctx, inflater, tts!!, grade) {
                        correctAnswerDesc = askNewQuestion()
                    }
                }
            }
        } else {
            Log.d(TAG, "Wrong answer detected, ignoring silently")
        }
    }

    private fun onWrongAttempt(questionKey: String) {
        if (lastQuestionKey == questionKey) {
            wrongAttemptsForCurrentQuestion++
            totalWrongAttemptsForCurrentQuestion++
        } else {
            lastQuestionKey = questionKey
            wrongAttemptsForCurrentQuestion = 1
            totalWrongAttemptsForCurrentQuestion = 1
            correctAnswerDialogCount = 0
        }

        if (wrongAttemptsForCurrentQuestion >= MAX_WRONG_ATTEMPTS) {
            correctAnswerDialogCount++

            context?.let { ctx ->
                activity?.layoutInflater?.let { inflater ->
                    val correctAnswerText = "The correct answer is $answer."
                    DialogUtils.showCorrectAnswerDialog(ctx, inflater, tts!!, correctAnswerText) {
                        if (totalWrongAttemptsForCurrentQuestion < 6) {
                            wrongAttemptsForCurrentQuestion = 0
                        } else {
                            fullyFailedQuestionCount++
                            if (fullyFailedQuestionCount >= 3) {
                                endGameWithScore()
                                return@showCorrectAnswerDialog
                            }

                            wrongAttemptsForCurrentQuestion = 0
                            totalWrongAttemptsForCurrentQuestion = 0
                            correctAnswerDialogCount = 0
                            lastQuestionKey = null
                            correctAnswerDesc = askNewQuestion()
                        }
                    }
                }
            }
            return
        }

        context?.let { ctx ->
            activity?.layoutInflater?.let { inflater ->
                DialogUtils.showRetryDialog(ctx, inflater, tts!!, getString(R.string.wrong_answer)) {}
            }
        }
    }

    private fun setupUI() {
        binding?.numberLineQuestion?.setOnClickListener {
            tts?.speak(questionDesc)
        }

        binding?.btnLeft?.setOnClickListener {
            viewModel.moveLeft()
        }

        binding?.btnRight?.setOnClickListener {
            viewModel.moveRight()
        }
    }

    private fun askNewQuestion(): String {
        // End the game if all questions are completed
        if (currentExcelQuestionIndex >= excelQuestions.size) {
            endGameWithScore()
            return ""
        }

        questionStartTime = System.currentTimeMillis()

        val question = excelQuestions[currentExcelQuestionIndex++]
        questionDesc = question.expression
        answer = question.answer

        val questionBrief = question.expression.take(40)
        binding?.numberLineQuestion?.text = questionBrief
        tts?.speak(questionDesc)

        return "${question.expression} = ${question.answer}"
    }


    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "numberline")
        }

        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        tts?.stop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}
