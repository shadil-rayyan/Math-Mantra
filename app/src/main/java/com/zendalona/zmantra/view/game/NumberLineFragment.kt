package com.zendalona.zmantra.view.game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.ViewModelProvider
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.databinding.FragmentGameNumberLineBinding
import com.zendalona.zmantra.domain.model.GameQuestion
import com.zendalona.zmantra.viewModel.NumberLineViewModel

class NumberLineFragment : BaseGameFragment() {

    private var binding: FragmentGameNumberLineBinding? = null
    private lateinit var viewModel: NumberLineViewModel

    private val handler = Handler(Looper.getMainLooper())

    private var questions: List<GameQuestion> = emptyList()
    private var currentIndex = 0
    private var answer = 0
    private var questionDesc = ""
    private var questionStartTime: Long = 0

    private var currentPosLabel: String = ""
    private var answerCheckRunnable: Runnable? = null

    override fun getModeName(): String = "numberline"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NumberLineViewModel::class.java]
        currentPosLabel = getString(R.string.current_position_label)
        tts.setSpeechRate(0.8f)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameNumberLineBinding.inflate(inflater, container, false)
        setupUI()
        setupObservers()
        return binding!!.root
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = if (questions.isEmpty()) listOf(GameQuestion("Move to 3", 3)) else questions
        askNextQuestion()
    }

    private fun setupUI() {
        binding?.apply {
            numberLineQuestion.setOnClickListener { tts.speak(questionDesc) }
            btnLeft.setOnClickListener { viewModel.moveLeft() }
            btnRight.setOnClickListener { viewModel.moveRight() }
        }
    }

    private fun setupObservers() {
        val updateUI = {
            val start = viewModel.lineStart.value ?: -5
            val end = viewModel.lineEnd.value ?: 5
            val position = viewModel.currentPosition.value ?: 0

            binding?.numberLineView?.updateNumberLine(start, end, position)
            binding?.currentPositionTv?.apply {
                text = "$currentPosLabel $position"
                contentDescription = "$currentPosLabel $position"
                post { announceForAccessibility(contentDescription.toString()) }
            }

            answerCheckRunnable?.let { handler.removeCallbacks(it) }
            answerCheckRunnable = Runnable { checkAnswer(position) }
            handler.postDelayed(answerCheckRunnable!!, 2000)
        }

        viewModel.lineStart.observe(viewLifecycleOwner) { updateUI() }
        viewModel.lineEnd.observe(viewLifecycleOwner) { updateUI() }
        viewModel.currentPosition.observe(viewLifecycleOwner) { updateUI() }
    }

    private fun checkAnswer(currentPosition: Int) {
        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0
        if (currentPosition == answer) {
            val grade = getGrade(elapsedTime, 15.0)
            showResultDialog(grade) {
                askNextQuestion()
            }
        }
        // else: do nothing, wait for user to correct
    }

    private fun askNextQuestion() {
        if (currentIndex >= questions.size) {
            endGame()
            return
        }

        val question = questions[currentIndex++]
        questionDesc = question.expression
        answer = question.answer
        questionStartTime = System.currentTimeMillis()

        binding?.numberLineQuestion?.apply {
            text = questionDesc.take(40)
            contentDescription = questionDesc
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            post {
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                announceForAccessibility(questionDesc)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        answerCheckRunnable?.let { handler.removeCallbacks(it) }
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
