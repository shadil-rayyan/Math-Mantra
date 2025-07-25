package com.zendalona.zmantra.view.base

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.view.HintFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseGameFragment : Fragment(), Hintable {

    protected lateinit var tts: TTSUtility
    protected lateinit var lang: String
    protected lateinit var difficulty: String
    protected open val mode: String by lazy { getModeName() }

    protected var attemptCount = 0
    protected open val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = LocaleHelper.getLanguage(requireContext()).ifEmpty { "en" }
        difficulty = DifficultyPreferences.getDifficulty(requireContext()).toString()
        tts = TTSUtility(requireContext())
        setHasOptionsMenu(true)
        loadQuestions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    override fun showHint() {
        val bundle = Bundle().apply { putString("mode", mode) }
        val hintFragment = HintFragment().apply { arguments = bundle }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    protected fun announce(view: View?, message: String) {
        val am = view?.context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        if (am?.isEnabled == true && am.isTouchExplorationEnabled) {
            view.contentDescription = message
            view.announceForAccessibility(message)
        }
    }

    protected fun announceNextQuestion(view: View?) {
        announce(view, getString(R.string.moving_to_next_question))
    }

    protected fun endGame() {
        announce(requireView(), getString(R.string.shake_game_over))
        endGameWithScore()
    }

    protected fun getGrade(elapsedTime: Double, limit: Double): String {
        return GradingUtils.getGrade(elapsedTime, limit, true)
    }

    private fun loadQuestions() {
        viewLifecycleOwner.lifecycleScope.launch {
            val questions = withContext(Dispatchers.IO) {
                loadGameQuestions(requireContext(), lang, mode, difficulty)
            }
            onQuestionsLoaded(questions)
        }
    }

    protected open suspend fun loadGameQuestions(
        context: Context,
        lang: String,
        mode: String,
        difficulty: String
    ): List<GameQuestion> {
        return ExcelQuestionLoader.loadQuestionsFromExcel(context, lang, mode, difficulty)
    }

    protected open fun showResultDialog(grade: String, onDismiss: () -> Unit) {
        DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade, onDismiss)
    }

    protected open fun showRetryDialog(onDismiss: () -> Unit) {
        DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.tap_failure), onDismiss)
    }

    protected open fun showNextDialog(onDismiss: () -> Unit) {
        DialogUtils.showNextDialog(requireContext(), layoutInflater, tts, getString(R.string.moving_to_next_question), onDismiss)
    }


    protected open fun showCorrectAnswerDialog(answer: String, onDismiss: () -> Unit) {
        DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater, tts, answer, onDismiss)
    }

    /**
     * Handles answer submission logic.
     * Can be called by any fragment extending this class.
     */
    protected fun handleAnswerSubmission(
        userAnswer: String,
        correctAnswer: String,
        elapsedTime: Double,
        timeLimit: Double,
        onCorrect: () -> Unit,
        onIncorrect: () -> Unit,
        onShowCorrect: (String) -> Unit
    ) {
        if (userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)) {
            attemptCount = 0
            val grade = getGrade(elapsedTime, timeLimit)
            showResultDialog(grade) {
                onCorrect()
            }
        } else {
            attemptCount++
            showRetryDialog {
                onIncorrect()
            }

            if (attemptCount >= maxAttempts) {
                attemptCount = 0
                showCorrectAnswerDialog(correctAnswer) {
                    onShowCorrect(correctAnswer)
                }
            }
        }
    }


    /** Must be implemented to handle loaded questions */
    protected abstract fun onQuestionsLoaded(questions: List<GameQuestion>)

    /** Must be implemented to return mode name like "shake", "tap" etc. */
    protected abstract fun getModeName(): String
}
