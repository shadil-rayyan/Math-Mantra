package com.zendalona.zmantra.view.game

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.zendalona.zmantra.R
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.utility.game.angle.RotationSensorUtility
import com.zendalona.zmantra.view.base.BaseGameFragment

class AngleFragment : BaseGameFragment() {

    private lateinit var rotationTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var rotationSensorUtility: RotationSensorUtility

    private var targetRotation = 0f
    private var baseAzimuth = -1f
    private var questionAnswered = false
    private var currentIndex = 0
    private var questions: List<GameQuestion> = emptyList()

    private lateinit var angleUpdateHandler: Handler
    private var angleUpdateRunnable: Runnable? = null
    private var holdRunnable: Runnable? = null
    private var isHolding = false
//    override fun getGifImageView(): ImageView? = binding?.animatedView
    override fun getGifResource(): Int = R.drawable.game_angle_rotateyourphone

    override fun getModeName(): String = "angle"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game_angle, container, false)

        rotationTextView = view.findViewById(R.id.rotation_angle_text)
        questionTextView = view.findViewById(R.id.angle_question)

        rotationSensorUtility = RotationSensorUtility(requireContext(), object : RotationSensorUtility.RotationListener {
            override fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float) {
                handleRotationChange(azimuth)
            }
        })

        angleUpdateHandler = Handler(Looper.getMainLooper())
        return view
    }

    override fun onStart() {
        super.onStart()
        rotationSensorUtility.registerListener()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGifIfDefined()
    }

    override fun onStop() {
        super.onStop()
        rotationSensorUtility.unregisterListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rotationSensorUtility.unregisterListener()
        angleUpdateRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
        holdRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = questions
        currentIndex = 0
        baseAzimuth = -1f
        questionAnswered = false
        isHolding = false

        if (questions.isEmpty()) {
            questionTextView.text = getString(R.string.no_questions_available)
        } else {
            showNextQuestion()
        }
    }

    private fun showNextQuestion() {
        if (currentIndex >= questions.size) {
            questionTextView.text = getString(R.string.game_finished)
            endGame()
            return
        }

        val question = questions[currentIndex++]
        targetRotation = question.answer.toFloat()
        questionAnswered = false
        isHolding = false
        baseAzimuth = -1f

        questionTextView.text = question.expression
        announce(questionTextView, question.expression)

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

    private fun handleRotationChange(azimuth: Float) {
        if (baseAzimuth < 0) {
            baseAzimuth = azimuth
            return
        }

        val relativeAzimuth = (azimuth - baseAzimuth + 360) % 360
        rotationTextView.text = getString(R.string.relative_angle_template, relativeAzimuth.toInt())
        validateAngle(relativeAzimuth)
    }

    private fun validateAngle(currentAngle: Float) {
        if (questionAnswered) return

        val withinRange = kotlin.math.abs(targetRotation - currentAngle) <= 5

        if (withinRange) {
            if (!isHolding) {
                isHolding = true
                holdRunnable = Runnable {
                    if (isHolding) {
                        questionAnswered = true
                        attemptCount = 0

                        val grade = getGrade(elapsedTime = 1.0, limit = 10.0) // You can calculate actual time if needed
                        showResultDialog(grade) {
                            showNextQuestion()
                        }
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

}
