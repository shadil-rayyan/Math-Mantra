package com.zendalona.zmantra.presentation.features.game.touchscreen

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.core.utility.accessibility.AccessibilityHelper
import com.zendalona.zmantra.databinding.FragmentGameTouchScreenBinding
import com.zendalona.zmantra.domain.model.GameQuestion

class TouchScreenFragment : BaseGameFragment() {

    private var binding: FragmentGameTouchScreenBinding? = null
    private var index = 0
    private var inputLocked = false
    private var questionStartTime = 0L
    private var correctAnswer = 0
    private var questionList: List<GameQuestion> = emptyList()
    override fun getGifImageView(): ImageView? = binding?.animatedView
    override fun getGifResource(): Int = R.drawable.game_touchthescreen
    private var isFirstQuestion = true
    private val handler = Handler(Looper.getMainLooper())

    override fun getModeName(): String = "touch"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameTouchScreenBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGifIfDefined()
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questionList = questions
        index = 0
        startGame()
    }

    private fun startGame() {
        if (index >= questionList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGame()
            return
        }

        inputLocked = false
        val question = questionList[index]
        correctAnswer = question.answer

        // Skip invalid questions (only skip if below)
        if (correctAnswer < 3) {
            index++
            startGame()
            return
        }

        questionStartTime = System.currentTimeMillis()

        // Take the expression exactly as stored in the question (no parsing)
        val expressionText = question.expression.trim()

        // Localized format: "Touch the screen with 3+2 fingers" or "Touch the screen with 5+4+1 fingers"
        val speakText = getString(
            R.string.touch_instruction,
            expressionText
        )

        binding?.angleQuestion?.apply {
            text = speakText
            requestFocus()
            contentDescription = speakText
            announceForAccessibility(speakText)
        }

        if (isFirstQuestion) {
            binding?.angleQuestion?.requestFocus()
            isFirstQuestion = false
        }

        setupTouchListener(question)
    }

    private fun setupTouchListener(question: GameQuestion) {
        binding?.root?.setOnTouchListener { _, event ->
            val pointerCount = event.pointerCount

            if ((event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) && !inputLocked) {
                binding?.angleQuestion?.text =
                    getString(R.string.touchscreen_fingers_on_screen, pointerCount)

                if (pointerCount == correctAnswer) {
                    inputLocked = true
                    handleCorrectAnswer(question)
                }
            }

            if (event.action == MotionEvent.ACTION_UP && !inputLocked) {
                inputLocked = true
                handleIncorrectAnswer(question)
            }

            true
        }
    }

    private fun handleCorrectAnswer(question: GameQuestion) {
        val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val grade = getGrade(elapsedSeconds, question.timeLimit.toDouble())

        if (question.celebration) {
            MediaPlayer.create(context, R.raw.bell_ring)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        }

        showResultDialog(grade) {
            index++
            startGame()
        }
    }

    private fun handleIncorrectAnswer(question: GameQuestion) {
        attemptCount++
        if (attemptCount >= maxAttempts) {
            attemptCount = 0
            showCorrectAnswerDialog(correctAnswer.toString()) {
                index++
                startGame()
            }
        } else {
            showRetryDialog {
                inputLocked = false
            }
        }
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
        binding = null
    }
}
