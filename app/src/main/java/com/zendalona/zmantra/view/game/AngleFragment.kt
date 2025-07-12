package com.zendalona.zmantra.view.game

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.DialogResultBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.game.angle.RotationSensorUtility
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.view.HintFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AngleFragment : Fragment(), RotationSensorUtility.RotationListener, Hintable {

    private lateinit var rotationTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var rotationSensorUtility: RotationSensorUtility

    private var targetRotation = 0f
    private var baseAzimuth = -1f
    private var questionAnswered = false
    private lateinit var angleUpdateHandler: Handler
    private var angleUpdateRunnable: Runnable? = null

    private var holdStartTime: Long = 0
    private var holdRunnable: Runnable? = null
    private var isHolding = false

    private var angleQuestions: List<GameQuestion> = emptyList()
    private var currentAngleQuestionIndex = 0

    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game_angle, container, false)

        rotationTextView = view.findViewById(R.id.rotation_angle_text)
        questionTextView = view.findViewById(R.id.angle_question)

        rotationSensorUtility = RotationSensorUtility(requireContext(), this)
        angleUpdateHandler = Handler(Looper.getMainLooper())
        setHasOptionsMenu(true)

        // ✅ Load angle questions asynchronously
        loadQuestionsAsync()

        return view
    }

    // Load questions asynchronously using coroutine
    private fun loadQuestionsAsync() {
        val difficulty = DifficultyPreferences.getDifficulty(requireContext())
        val lang = LocaleHelper.getLanguage(context) ?: "en"

        lifecycleScope.launch {

            angleQuestions = withContext(Dispatchers.IO) {
                ExcelQuestionLoader.loadQuestionsFromExcel(
                    requireContext(),
                    lang = lang,
                    mode = "angle",
                    difficulty = difficulty.toString()
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rotationSensorUtility.unregisterListener()
        angleUpdateRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
    }

    override fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float) {
        if (baseAzimuth < 0) {
            baseAzimuth = azimuth
            generateNewQuestion()
            return
        }

        val relativeAzimuth = (azimuth - baseAzimuth + 360) % 360

        if (rotationTextView != null && isAdded) {
            requireActivity().runOnUiThread {
                val angleText = getString(R.string.relative_angle_template, relativeAzimuth.toInt())
                rotationTextView.text = angleText
                checkIfCorrect(relativeAzimuth)
            }
        }
    }

    private fun checkIfCorrect(currentAngle: Float) {
        if (questionAnswered) return

        val withinRange = Math.abs(targetRotation - currentAngle) <= 5

        if (withinRange) {
            if (!isHolding) {
                isHolding = true
                holdStartTime = System.currentTimeMillis()

                holdRunnable = Runnable {
                    if (isHolding) {
                        questionAnswered = true
                        showResultDialog(true)
                    }
                }
                angleUpdateHandler.postDelayed(holdRunnable!!, 3000)
            }
        } else {
            if (isHolding) {
                isHolding = false
                holdRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
            }
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val messageResId = if (isCorrect) R.string.right_answer else R.string.wrong_answer
        val gifResId = if (isCorrect) R.drawable.right else R.drawable.wrong

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        Glide.with(this)
            .asGif()
            .load(gifResId)
            .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = getString(messageResId)
        dialogView.announceForAccessibility(getString(messageResId))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                generateNewQuestion()
            }
        }, 4000)
    }

    private fun generateNewQuestion() {
        if (currentAngleQuestionIndex >= angleQuestions.size) {
            questionTextView.text = getString(R.string.game_finished)
            return
        }

        val question = angleQuestions[currentAngleQuestionIndex++]
        targetRotation = question.answer.toFloat()
        questionAnswered = false

        questionTextView.text = question.expression

        if (AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
            questionTextView.announceForAccessibility(question.expression)
        }

        if (angleUpdateRunnable == null) {
            angleUpdateRunnable = object : Runnable {
                override fun run() {
                    if (!questionAnswered && isAdded && AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                        val spokenAngle = rotationTextView.text.toString()
                        val announcement = getString(R.string.current_angle_announcement, spokenAngle)
                        rotationTextView.announceForAccessibility(announcement)
                        angleUpdateHandler.postDelayed(this, 3000)
                    }
                }
            }
        }

        angleUpdateHandler.postDelayed(angleUpdateRunnable!!, 3000)
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "angle")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }


}
