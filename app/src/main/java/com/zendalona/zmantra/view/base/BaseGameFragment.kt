package com.zendalona.zmantra.view.base

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.excel.QuestionCache
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

    protected var attemptCount = 0
    protected open val maxAttempts = 3
    open fun getGifImageView(): ImageView? = null
    open fun getGifResource(): Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = LocaleHelper.getLanguage(requireContext()).ifEmpty { "en" }
        difficulty = DifficultyPreferences.getDifficulty(requireContext()).toString()
        tts = TTSUtility(requireContext())
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadQuestions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
    }

    fun loadGifIfDefined() {
        val imageView = getGifImageView()
        val gifResId = getGifResource()
        if (imageView != null && gifResId != null && gifResId != 0) {
            Glide.with(requireContext())
                .asGif()
                .load(gifResId)
                .into(imageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", getModeName())
        }
        val hintFragment = HintFragment().apply {
            arguments = bundle
        }
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
            val start = System.currentTimeMillis()

            val lang = this@BaseGameFragment.lang
            val difficulty = this@BaseGameFragment.difficulty
            val mode = getModeName() // ⏳ lazy mode evaluation

            val questions = withContext(Dispatchers.IO) {
                loadGameQuestions(requireContext(), lang, mode, difficulty)
            }

            val end = System.currentTimeMillis()
            android.util.Log.d("BaseGameFragment", "Loaded ${questions.size} questions in ${end - start} ms")

            onQuestionsLoaded(questions)
        }
    }

    protected open suspend fun loadGameQuestions(
        context: Context,
        lang: String,
        mode: String,
        difficulty: String
    ): List<GameQuestion> {
        // ✅ Try from cache first (fast)
        val cached = QuestionCache.getQuestions(lang, mode, difficulty)
        if (cached.isNotEmpty()) return cached

        // ❌ If not found in cache, fallback to Excel (slow)
        return ExcelQuestionLoader.loadQuestionsFromExcel(context, lang, mode, difficulty)
    }

    protected open fun showResultDialog(grade: String, onDismiss: () -> Unit) {
        DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade, onDismiss)
    }

    protected open fun showRetryDialog(onDismiss: () -> Unit) {
        DialogUtils.showRetryDialog(
            requireContext(), layoutInflater, tts,
            getString(R.string.tap_failure), onDismiss
        )
    }

    protected open fun showNextDialog(onDismiss: () -> Unit) {
        DialogUtils.showNextDialog(
            requireContext(), layoutInflater, tts,
            getString(R.string.moving_to_next_question), onDismiss
        )
    }

    protected open fun showCorrectAnswerDialog(answer: String, onDismiss: () -> Unit) {
        DialogUtils.showCorrectAnswerDialog(
            requireContext(), layoutInflater, tts, answer, onDismiss
        )
    }

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
            showResultDialog(grade) { onCorrect() }
        } else {
            attemptCount++
            showRetryDialog { onIncorrect() }

            if (attemptCount >= maxAttempts) {
                attemptCount = 0
                showCorrectAnswerDialog(correctAnswer) { onShowCorrect(correctAnswer) }
            }
        }
    }

    protected abstract fun onQuestionsLoaded(questions: List<GameQuestion>)
    protected abstract fun getModeName(): String
}
