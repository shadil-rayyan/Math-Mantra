package com.zendalona.zmantra.view.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameTouchScreenBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.zmantra.utility.common.*
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.model.Hintable
import kotlinx.coroutines.launch
class TouchScreenFragment : Fragment(), Hintable {

    private var binding: FragmentGameTouchScreenBinding? = null
    private lateinit var tts: TTSUtility
    private val handler = Handler(Looper.getMainLooper())

    private var index = 0
    private var wrongAttempts = 0
    private var questionStartTime = 0L
    private var correctAnswer = 0
    private var inputLocked = false

    private lateinit var lang: String
    private var questionList: List<GameQuestion> = emptyList() // Make it safely initialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TTSUtility(requireContext())
        lang = LocaleHelper.getLanguage(requireContext()).ifEmpty { "en" }
        val difficulty = DifficultyPreferences.getDifficulty(requireContext()).toString()

        lifecycleScope.launch {
            // Load questions asynchronously and shuffle them
            questionList = ExcelQuestionLoader.loadQuestionsFromExcel(
                requireContext(), lang, "touch", difficulty
            ).shuffled()

            if (questionList.isNotEmpty()) {
                startGame()  // Start the game once data is ready
            } else {
                // Handle empty question list scenario
                Toast.makeText(
                    requireContext(),
                    "No touch questions found in Excel for $lang / $difficulty.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameTouchScreenBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun startGame() {
        if (index >= questionList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
            return
        }

        inputLocked = false
        val question = questionList[index]
        correctAnswer = question.answer

        // Skip the question if the correct answer is 3
        if (correctAnswer == 3) {
            index++
            startGame()  // Skip to the next question
            return
        }

        questionStartTime = System.currentTimeMillis()

        // Use the localized touch instruction string
        val readableExpr = question.expression
        val speakText = getString(R.string.touch_instruction, question.answer, readableExpr)

        binding?.angleQuestion?.apply {
            text = speakText
           requestFocus()
            contentDescription = speakText
            announceForAccessibility(speakText)
        }

        setupTouchListener()
    }

    private fun setupTouchListener() {
        binding?.root?.setOnTouchListener { _, event ->
            val pointerCount = event.pointerCount

            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                binding?.angleQuestion?.text =
                    getString(R.string.touchscreen_fingers_on_screen, pointerCount)

                if (pointerCount == correctAnswer && !inputLocked) {
                    inputLocked = true
                    evaluateGameResult(success = true)
                }
            }

            if (event.action == MotionEvent.ACTION_UP && !inputLocked) {
                inputLocked = true
                evaluateGameResult(success = false)
            }

            true
        }
    }

    private fun evaluateGameResult(success: Boolean) {
        handler.postDelayed({
            val question = questionList[index]
            val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
            val grade = GradingUtils.getGrade(
                elapsedSeconds, question.timeLimit.toDouble(), success
            )

            if (success) {
                wrongAttempts = 0
                if (question.celebration) {
                    MediaPlayer.create(context, R.raw.bell_ring)?.apply {
                        setOnCompletionListener { release() }
                        start()
                    }
                }

                DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                    index++
                    startGame()
                }

            } else {
                wrongAttempts++
                if (wrongAttempts >= 3) {
                    tts.speak(getString(R.string.shake_game_over))
                    endGameWithScore()
                } else {
                    DialogUtils.showRetryDialog(
                        requireContext(), layoutInflater, tts,
                        getString(R.string.shake_failure)
                    ) {
                        startGame()
                    }
                }
            }
        }, 500)
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "touch")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.disableExploreByTouch(it)
        }
    }

    override fun onPause() {
        super.onPause()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.resetExploreByTouch(it)
        }
        handler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.shutdown()
        binding = null
    }
}
