package com.zendalona.zmantra.presentation.features.game.drawing

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.core.customView.DrawingView
import com.zendalona.zmantra.databinding.FragmentGameDrawingBinding
import com.zendalona.zmantra.domain.model.GameQuestion

class DrawingFragment : BaseGameFragment() {

    private var _binding: FragmentGameDrawingBinding? = null
    private val binding get() = _binding!!

    private var drawingView: DrawingView? = null
    private var isFirstQuestion = true
    private var currentQuestion: GameQuestion? = null
    private var currentIndex = 0
    private var questions: List<GameQuestion> = emptyList()

    override fun getModeName(): String = "drawing"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDrawingBinding.inflate(inflater, container, false)

        drawingView = DrawingView(requireContext())
        binding.drawingContainer.addView(drawingView)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resetButton.setOnClickListener {
            drawingView?.clearCanvas()
            announce(binding.root, getString(R.string.canvas_cleared))
        }

        binding.submitButton.setOnClickListener {
            checkAnswer()
        }
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = questions
        if (questions.isEmpty()) {
            binding.questionText.text = getString(R.string.no_questions_available)
            binding.submitButton.isEnabled = false
            binding.resetButton.isEnabled = false
        } else {
            currentIndex = 0
            loadQuestionAt(currentIndex, this.questions)
        }
    }

    private fun loadQuestionAt(index: Int, questions: List<GameQuestion>) {
        if (index >= questions.size) {
            announce(binding.root, getString(R.string.task_completed))
            Handler(Looper.getMainLooper()).postDelayed(
                { requireActivity().onBackPressedDispatcher.onBackPressed() },
                3000
            )
            return
        }

        currentQuestion = questions[index]
        val shape = currentQuestion?.expression ?: ""
        val instruction = getString(R.string.drawing_task, shape)

        binding.questionText.apply {
            text = instruction
            requestFocus()
            contentDescription = instruction
            announceForAccessibility(instruction)
        }
        if (isFirstQuestion) {
            binding?.questionText?.requestFocus()
            isFirstQuestion = false
        }

        drawingView?.clearCanvas()
        attemptCount = 0 // reset attempts for new question
    }

    private fun checkAnswer() {
        val message = getString(R.string.moving_to_next_question)
        showNextDialog {
            currentIndex++
            loadQuestionAt(currentIndex, questions)
        }
        announce(binding.root, message)
    }
}
