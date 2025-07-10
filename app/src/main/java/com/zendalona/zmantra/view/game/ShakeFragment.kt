package com.zendalona.zmantra.view.game

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameShakeBinding
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.utility.AccelerometerUtility
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.common.*
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import kotlinx.coroutines.*

// Extension function for accessibility announcement
fun View.announceForAccessibilityCompat(message: String) {
    contentDescription = message
    val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    event.text.add(message)
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    am.sendAccessibilityEvent(event)
}

class ShakeFragment : Fragment(), Hintable {

    private var binding: FragmentGameShakeBinding? = null
    private lateinit var accelerometerUtility: AccelerometerUtility
    private lateinit var tts: TTSUtility

    private var count = 0
    private var target = 0
    private var isShakingAllowed = true
    private val shakeHandler = Handler()
    private val gameHandler = Handler(Looper.getMainLooper())

    private var index = 0
    private var retryCount = 0
    private var failCountOnQuestion = 0
    private var totalFailedQuestions = 0
    private var firstShakeTime: Long = 0
    private var answerChecked = false
    private var isFirstOpen = true

    private lateinit var lang: String
    private lateinit var difficulty: String
    private var parsedShakeList: List<GameQuestion> = emptyList()
    private var questionStartTime: Long = 0

    companion object {
        private const val TAG = "ShakeFragment"
    }

    private fun logFocusState(tag: String) {
        val focused = activity?.window?.decorView?.findFocus()
        Log.d(TAG, "$tag ➜ Focused view: ${focused?.id?.let { resources.getResourceEntryName(it) }}, contentDescription='${focused?.contentDescription}', isFocused=${focused?.isFocused}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        lang = LocaleHelper.getLanguage(requireContext())
        val difficultyNum = DifficultyPreferences.getDifficulty(requireContext())
        difficulty = difficultyNum.toString()
        Log.d(TAG, "Language: $lang, Difficulty: $difficulty")

        tts = TTSUtility(requireContext())
        accelerometerUtility = AccelerometerUtility(requireContext())

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                parsedShakeList = ExcelQuestionLoader.loadQuestionsFromExcel(
                    requireContext(), lang, "shake", difficulty
                )
            }
            Log.d(TAG, "Loaded ${parsedShakeList.size} questions")
            startGame()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView called")
        binding = FragmentGameShakeBinding.inflate(inflater, container, false)
        Handler(Looper.getMainLooper()).postDelayed({
            setHasOptionsMenu(true)
        }, 2000)
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logFocusState("onViewCreated")

        if (isFirstOpen) {
            binding?.rootLayout?.apply {
                requestFocus()
                postDelayed({
                    logFocusState("announceForAccessibility - first open")
                    announceForAccessibilityCompat(contentDescription?.toString() ?: "")
                }, 500)
            }
            isFirstOpen = false
        }
    }

    private fun startGame() {
        Log.d(TAG, "🔄 startGame() called at index $index")
        logFocusState("startGame ENTRY")

        binding?.apply {
            listOf(rootLayout, ringCount, ringMeTv).forEach {
                it.clearFocus()
                it.contentDescription = null
                it.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }

            // Shift focus temporarily to dummyFocusView to clear old focus
            dummyFocusView?.apply {
                contentDescription = ""
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                isFocusable = true
                visibility = View.VISIBLE
                requestFocus()
                logFocusState("dummyFocusView requested focus")
            }
        }

        if (parsedShakeList.isEmpty()) {
            Toast.makeText(requireContext(), "No questions available", Toast.LENGTH_LONG).show()
            tts.speak("No questions available.")
            Handler(Looper.getMainLooper()).post {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            return
        }

        count = 0
        firstShakeTime = 0L
        answerChecked = false
        binding?.ringCount?.text = getString(R.string.shake_count_initial)

        val question = parsedShakeList[index % parsedShakeList.size]
        target = question.answer
        questionStartTime = System.currentTimeMillis()

        val instruction = getString(R.string.shake_target_expression, question.expression)

        // Delay to allow focus settling on dummy view before announcing
        binding?.ringMeTv?.postDelayed({
            binding?.ringMeTv?.apply {
                text = instruction
                contentDescription = instruction
                isFocusable = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
                requestFocus()
                logFocusState("before announceForAccessibility (instruction)")
                announceForAccessibilityCompat(instruction)
                logFocusState("after announceForAccessibility (instruction)")
            }
        }, 400)
    }

    private fun onShakeDetected() {
        if (!isShakingAllowed) {
            Log.d(TAG, "Shake ignored, not allowed yet")
            return
        }

        isShakingAllowed = false
        shakeHandler.postDelayed({ isShakingAllowed = true }, 500)

        count++
        Log.d(TAG, "Shake detected. Count: $count")
        binding?.ringCount?.text = count.toString()
        tts.stop()

        if (firstShakeTime == 0L) {
            Log.d(TAG, "First shake. Starting timer to check answer")
            firstShakeTime = System.currentTimeMillis()
            answerChecked = false
        }

        if (count == target && !answerChecked) {
            Log.d(TAG, "Target reached. Checking answer immediately")
            checkAnswer()
        }
    }

    private fun checkAnswer(forceWrong: Boolean = false) {
        if (answerChecked) return

        answerChecked = true
        val question = parsedShakeList[index % parsedShakeList.size]
        val isCorrect = !forceWrong && count == target

        if (isCorrect) {
            retryCount = 0
            failCountOnQuestion = 0
            totalFailedQuestions = 0

            if (question.celebration) {
                MediaPlayer.create(context, R.raw.bell_ring)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }

            val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
            val grade = GradingUtils.getGrade(elapsedSeconds, question.timeLimit.toDouble(), true)
            DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
                nextOrEnd()
            }
        } else {
            retryCount++
            failCountOnQuestion++

            if (failCountOnQuestion >= 6) {
                totalFailedQuestions++
                if (totalFailedQuestions >= 3) {
                    tts.speak(getString(R.string.shake_game_over))
                    endGameWithScore()
                    return
                } else {
                    DialogUtils.showNextDialog(requireContext(), layoutInflater, tts, getString(R.string.moving_to_next_question)) {
                        binding?.ringCount?.text = getString(R.string.shake_count_initial)
                    }
                    return
                }
            }

            if (retryCount >= 3) {
                DialogUtils.showCorrectAnswerDialog(requireContext(), layoutInflater, tts, "${question.answer}") {}
            } else {
                DialogUtils.showRetryDialog(requireContext(), layoutInflater, tts, getString(R.string.shake_failure)) {
                    count = 0
                    firstShakeTime = 0L
                    answerChecked = false
                    binding?.ringCount?.text = getString(R.string.shake_count_initial)
                }
            }
        }

        if (answerChecked) {
            count = 0
            firstShakeTime = 0L
        }
    }

    private fun nextOrEnd() {
        index++
        Log.d(TAG, "➡️ nextOrEnd to index: $index")
        logFocusState("nextOrEnd ENTRY")

        if (index >= parsedShakeList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
        } else {
            binding?.ringMeTv?.apply {
                contentDescription = null
                isFocusable = false
            }
            logFocusState("before startGame in nextOrEnd")
            startGame()
        }
    }

    override fun showHint() {
        val bundle = Bundle().apply { putString("mode", "shake") }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        accelerometerUtility.registerListener()
        isShakingAllowed = true

        shakeHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isVisible && accelerometerUtility.isDeviceShaken()) {
                    requireActivity().runOnUiThread { onShakeDetected() }
                }
                shakeHandler.postDelayed(this, 500)
            }
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        accelerometerUtility.unregisterListener()
        shakeHandler.removeCallbacksAndMessages(null)
        gameHandler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        binding = null
        tts.shutdown()
    }
}
