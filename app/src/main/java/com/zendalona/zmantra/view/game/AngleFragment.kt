package com.zendalona.zmantra.view.game

import android.app.AlertDialog
import android.content.Context
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
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.view.HintFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class AngleFragment : Fragment(), Hintable {

    private lateinit var rotationTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var rotationSensorUtility: RotationSensorUtility

    private var targetRotation = 0f
    private var baseAzimuth = -1f
    private var questionAnswered = false
    private lateinit var angleUpdateHandler: Handler
    private var angleUpdateRunnable: Runnable? = null

    private var holdRunnable: Runnable? = null
    private var isHolding = false

    private var angleQuestions: List<GameQuestion> = emptyList()
    private var currentAngleQuestionIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game_angle, container, false)

        rotationTextView = view.findViewById(R.id.rotation_angle_text)
        questionTextView = view.findViewById(R.id.angle_question)

        rotationSensorUtility = RotationSensorUtility(requireContext(), object : RotationSensorUtility.RotationListener {
            override fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float) {
                onRotationChanged(azimuth)
            }
        })

        angleUpdateHandler = Handler(Looper.getMainLooper())
        setHasOptionsMenu(true)

        loadQuestionsAsync()

        return view
    }

    private fun loadQuestionsAsync() {
        val difficultyNum = DifficultyPreferences.getDifficulty(requireContext())
        val difficulty = difficultyNum.toString()
        val lang = LocaleHelper.getLanguage(context) ?: "en"

        lifecycleScope.launch {
            angleQuestions = withContext(Dispatchers.IO) {
                ExcelQuestionLoader.loadQuestionsFromExcel(
                    requireContext(), lang, "angle", difficulty
                )
            }

            Log.d("AngleFragment", "Loaded ${angleQuestions.size} questions")

            if (angleQuestions.isEmpty()) {
                questionTextView.text = getString(R.string.no_questions_available)
                return@launch
            }

            // Reset index and baseAzimuth when questions are loaded
            currentAngleQuestionIndex = 0
            baseAzimuth = -1f
            questionAnswered = false
            isHolding = false

            if (isAdded) {
                generateNewQuestion()
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
        holdRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
    }

    override fun onStart() {
        super.onStart()
        rotationSensorUtility.registerListener()
    }

    override fun onStop() {
        super.onStop()
        rotationSensorUtility.unregisterListener()
    }

    private fun onRotationChanged(azimuth: Float) {
        // Set base azimuth only once per question, do not call generateNewQuestion here
        if (baseAzimuth < 0) {
            baseAzimuth = azimuth
            return
        }

        val relativeAzimuth = (azimuth - baseAzimuth + 360) % 360

        if (isAdded) {
            rotationTextView.text = getString(R.string.relative_angle_template, relativeAzimuth.toInt())
            checkIfCorrect(relativeAzimuth)
        }
    }

    private fun checkIfCorrect(currentAngle: Float) {
        if (questionAnswered) return

        val withinRange = Math.abs(targetRotation - currentAngle) <= 5

        if (withinRange) {
            if (!isHolding) {
                isHolding = true

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
                if (isAdded) {
                    generateNewQuestion()
                }
            }
        }, 4000)
    }

    private fun generateNewQuestion() {
        if (angleQuestions.isEmpty()) {
            questionTextView.text = getString(R.string.no_questions_available)
            return
        }

        if (currentAngleQuestionIndex >= angleQuestions.size) {
            questionTextView.text = getString(R.string.game_finished)
            return
        }

        val question = angleQuestions[currentAngleQuestionIndex++]
        targetRotation = question.answer.toFloat()
        questionAnswered = false
        isHolding = false
        baseAzimuth = -1f  // Reset base azimuth so relative angle recalculates for this question

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

    class RotationSensorUtility(context: Context, private val listener: RotationListener) : SensorEventListener {

        private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        fun unregisterListener() {
            sensorManager.unregisterListener(this)
        }

        fun registerListener() {
            rotationSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        interface RotationListener {
            fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float)
        }

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                val pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()

                listener.onRotationChanged(azimuth, pitch, roll)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No-op
        }

        init {
            registerListener()
        }
    }
}

