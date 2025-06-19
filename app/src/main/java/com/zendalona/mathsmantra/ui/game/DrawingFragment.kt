package com.zendalona.mathsmantra.ui.game

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameDrawingBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences.getDifficulty
import com.zendalona.mathsmantra.utility.settings.LocaleHelper.getLanguage
import com.zendalona.mathsmantra.view.DrawingView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class DrawingFragment : Fragment(), Hintable {
    private var binding: FragmentGameDrawingBinding? = null
    private var drawingView: DrawingView? = null
    private var accessibilityManager: AccessibilityManager? = null

    private var shapeList: MutableList<String?> = ArrayList<String?>()
    private var currentIndex = 0
    private var lang = "en"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameDrawingBinding.inflate(inflater, container, false)
        val context = requireContext()
        setHasOptionsMenu(true)  // Tell system this Fragment wants menu callbacks

        accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        // Add the custom drawing view to the container
        drawingView = DrawingView(context)
        binding!!.drawingContainer.addView(drawingView)

        // Load language and difficulty
        val difficulty = getDifficulty(context)
        lang = getLanguage(context)
        if (TextUtils.isEmpty(lang)) lang = "en"

        shapeList = loadShapesFromAssets(lang, difficulty)

        setupListeners()
        loadNextShape()

        return binding!!.getRoot()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true  // Show hint here
    }

    private fun loadShapesFromAssets(lang: String?, difficulty: String): MutableList<String?> {
        val shapes: MutableList<String?> = ArrayList<String?>()
        val fileName = lang + "/game/drawing/" + difficulty.lowercase() + ".txt"

        try {
            BufferedReader(
                InputStreamReader(requireContext().getAssets().open(fileName))
            ).use { reader ->
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    if (!line!!.trim { it <= ' ' }.isEmpty()) {
                        shapes.add(line.trim { it <= ' ' })
                    }
                }
                if (shapes.isEmpty()) {
                    showToast("No shapes found in " + fileName)
                }
            }
        } catch (e: IOException) {
            showToast("Failed to load shapes from: " + fileName)
            e.printStackTrace()
        }

        return shapes
    }

    private fun setupListeners() {
        binding!!.resetButton.setOnClickListener(View.OnClickListener { v: View? ->
            drawingView!!.clearCanvas()
            announce(getString(R.string.canvas_cleared))
        })

        binding!!.submitButton.setOnClickListener(View.OnClickListener { v: View? ->
            showResultDialogAndNext() // No checking, just move ahead
        })
    }

    private fun loadNextShape() {
        if (currentIndex >= shapeList.size) {
            announce(getString(R.string.task_completed))
            Handler(Looper.getMainLooper()).postDelayed(
                Runnable { requireActivity().onBackPressed() },
                3000
            )
            return
        }

        val shape = shapeList.get(currentIndex)
        val instruction = getString(R.string.drawing_task, shape)

        binding!!.questionText.setText(instruction)
        binding!!.questionText.setContentDescription(instruction)
        binding!!.questionText.announceForAccessibility(instruction)
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
        if (accessibilityManager!!.isEnabled()) {
            binding!!.getRoot().announceForAccessibility(message)
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
            putString("mode", "drawing") // Pass only the mode
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
