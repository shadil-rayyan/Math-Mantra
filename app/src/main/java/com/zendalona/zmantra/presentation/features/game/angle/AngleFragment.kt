package com.zendalona.zmantra.presentation.features.game.angle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.core.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.core.utility.game.angle.RotationSensorUtility
import com.zendalona.zmantra.databinding.FragmentGameAngleBinding
import com.zendalona.zmantra.domain.model.GameQuestion

class AngleFragment : BaseGameFragment() {

    private var _binding: FragmentGameAngleBinding? = null
    private val binding get() = _binding!!

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

    override fun getGifResource(): Int = R.drawable.game_angle_rotateyourphone

    override fun getModeName(): String = "angle"

    override fun getGifImageView(): ImageView? = binding.animatedView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameAngleBinding.inflate(inflater, container, false)
        angleUpdateHandler = Handler(Looper.getMainLooper())

        rotationSensorUtility = RotationSensorUtility(requireContext(), object : RotationSensorUtility.RotationListener {
            override fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float) {
                handleRotationChange(azimuth)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGifIfDefined()
    }

    override fun onStart() {
        super.onStart()
        rotationSensorUtility.registerListener()
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
        _binding = null
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = questions
        currentIndex = 0
        baseAzimuth = -1f
        questionAnswered = false
        isHolding = false

        if (questions.isEmpty()) {
            binding.angleQuestion.text = getString(R.string.no_questions_available)
        } else {
            showNextQuestion()
        }
    }

    private fun showNextQuestion() {
        if (currentIndex >= questions.size) {
            binding.angleQuestion.text = getString(R.string.game_finished)
            endGame()
            return
        }

        val question = questions[currentIndex++]
        targetRotation = question.answer.toFloat()
        questionAnswered = false
        isHolding = false
        baseAzimuth = -1f

        val localizedText = getString(R.string.turn_phone_90_degrees, question.expression)

        binding.angleQuestion.apply {
            text = localizedText
            contentDescription = localizedText
            announceForAccessibility(localizedText)
        }




        if (angleUpdateRunnable == null) {
            angleUpdateRunnable = object : Runnable {
                override fun run() {
                    if (!questionAnswered && isAdded && AccessibilityUtils().isSystemExploreByTouchEnabled(requireContext())) {
                        val spokenAngle = binding.rotationAngleText.text.toString()
                        val announcement = getString(R.string.current_angle_announcement, spokenAngle)
                        binding.rotationAngleText.announceForAccessibility(announcement)
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
        binding.rotationAngleText.text = getString(R.string.relative_angle_template, relativeAzimuth.toInt())
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
                        val grade = getGrade(elapsedTime = 1.0, limit = 10.0)
                        showResultDialog(grade) {
                            showNextQuestion()
                        }
                    }
                }
                angleUpdateHandler.postDelayed(holdRunnable!!, 5000)
            }
        } else {
            if (isHolding) {
                isHolding = false
                holdRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
            }
        }
    }
}
