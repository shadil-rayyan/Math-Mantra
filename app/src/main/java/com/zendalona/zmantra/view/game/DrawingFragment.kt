package com.zendalona.zmantra.view.game

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameDrawingBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences.getDifficulty
import com.zendalona.zmantra.utility.settings.LocaleHelper.getLanguage
import com.zendalona.zmantra.customView.DrawingView

class DrawingFragment : Fragment(), Hintable {

    private var binding: FragmentGameDrawingBinding? = null
    private var drawingView: DrawingView? = null
    private var accessibilityManager: AccessibilityManager? = null

    private var questions: List<GameQuestion> = emptyList()
    private var currentIndex = 0
    private var lang = "en"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameDrawingBinding.inflate(inflater, container, false)
        val context = requireContext()
        setHasOptionsMenu(true)

        accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        drawingView = DrawingView(context)
        binding!!.drawingContainer.addView(drawingView)

        val difficulty = getDifficulty(context).toString()
        lang = getLanguage(context)
        if (TextUtils.isEmpty(lang)) lang = "en"

        questions = ExcelQuestionLoader.loadQuestionsFromExcel(context, lang, "drawing", difficulty)

        setupListeners()
        loadNextShape()

        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun setupListeners() {
        binding!!.resetButton.setOnClickListener {
            drawingView!!.clearCanvas()
            announce(getString(R.string.canvas_cleared))
        }

        binding!!.submitButton.setOnClickListener {
            showResultDialogAndNext()
        }
    }

    private fun loadNextShape() {
        if (currentIndex >= questions.size) {
            announce(getString(R.string.task_completed))
            Handler(Looper.getMainLooper()).postDelayed(
                { requireActivity().onBackPressedDispatcher.onBackPressed() },
                3000
            )
            return
        }

        val shape = questions[currentIndex].expression
        val instruction = getString(R.string.drawing_task, shape)

        binding!!.questionText.apply {
            text = instruction
            contentDescription = instruction
            announceForAccessibility(instruction)
        }

        drawingView!!.clearCanvas()
    }

    private fun showResultDialogAndNext() {
        val message = getString(R.string.moving_to_next_question)

        DialogUtils.showNextDialog(
            context = requireContext(),
            inflater = layoutInflater,
            ttsUtility = TTSUtility(requireContext()),
            message = message
        ) {
            currentIndex++
            loadNextShape()
        }

        announce(message)
    }

    private fun announce(message: String?) {
        if (accessibilityManager?.isEnabled == true) {
            binding!!.root.announceForAccessibility(message)
        }
    }

    private fun showToast(msg: String?) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        drawingView?.onResume()
    }

    override fun onPause() {
        drawingView?.onPause()
        super.onPause()
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "drawing")
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
    }
}
