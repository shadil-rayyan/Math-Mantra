package com.zendalona.zmantra.view.game

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameTouchScreenBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.zmantra.view.base.BaseGameFragment
import kotlinx.coroutines.launch

class TouchScreenFragment : BaseGameFragment() {

    private var binding: FragmentGameTouchScreenBinding? = null
    private var index = 0
    private var inputLocked = false
    private var questionStartTime = 0L
    private var correctAnswer = 0
    private var questionList: List<GameQuestion> = emptyList()
    override fun getGifImageView(): ImageView? = binding?.animatedView
    override fun getGifResource(): Int = R.drawable.game_touchthescreen

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

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questionList = questions.shuffled()
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

        // Skip invalid questions
        if (correctAnswer == 3) {
            index++
            startGame()
            return
        }

        questionStartTime = System.currentTimeMillis()

        val speakText = getString(R.string.touch_instruction, correctAnswer, question.expression)

        binding?.angleQuestion?.apply {
            text = speakText
            requestFocus()
            contentDescription = speakText
            announceForAccessibility(speakText)
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
