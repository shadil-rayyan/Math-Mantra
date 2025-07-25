package com.zendalona.zmantra.view.game

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameShakeBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.view.base.BaseGameFragment
import kotlin.math.sqrt
import kotlin.text.toDoubleOrNull

class ShakeFragment : BaseGameFragment(), SensorEventListener {

    private var _binding: FragmentGameShakeBinding? = null
    private val binding get() = _binding

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var index = 0
    private var count = 0
    private var answerChecked = false
    private var firstShakeTime = 0L

    private var isShakingAllowed = true
    private val shakeHandler = Handler(Looper.getMainLooper())
    private val gameHandler = Handler(Looper.getMainLooper())
    override fun getGifImageView(): ImageView? = binding?.animatedView
    override fun getGifResource(): Int = R.drawable.game_angle_rotateyourphone


    private var parsedShakeList: List<GameQuestion> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameShakeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        loadGifIfDefined()

    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getModeName(): String = "shake"

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        parsedShakeList = questions
        startQuestion()
    }

    private fun startQuestion() {
        if (parsedShakeList.isEmpty()) return

        val question = parsedShakeList[index % parsedShakeList.size]

        count = 0
        answerChecked = false
        firstShakeTime = 0L

        val instruction = getString(R.string.shake_target_expression, question.expression)
        binding?.ringMeTv?.text = instruction
        binding?.ringMeTv?.contentDescription = instruction
        announce(binding?.ringMeTv, instruction)

        binding?.ringCount?.text = getString(R.string.shake_count_initial)
    }

    private fun resetShake() {
        count = 0
        firstShakeTime = 0L
        answerChecked = false
        binding?.ringCount?.text = getString(R.string.shake_count_initial)
    }

    private fun proceedToNextQuestion() {
        index++
        if (index >= parsedShakeList.size) {
            endGame()
        } else {
            resetShake()
            startQuestion()
        }
    }

    private fun checkAnswer(forceWrong: Boolean = false) {
        if (answerChecked) return
        answerChecked = true

        val question = parsedShakeList[index % parsedShakeList.size]
        val userAnswer = count.toString()
        val correctAnswer = question.answer.toString()

        val elapsedSeconds = (System.currentTimeMillis() - firstShakeTime) / 1000.0
        val timeLimit = question.timeLimit.toDouble() ?: 10.0

        // ❗ Temporarily pause shake sensor when result dialog is being shown
        isShakingAllowed = false

        handleAnswerSubmission(
            userAnswer = if (forceWrong) "wrong" else userAnswer,
            correctAnswer = correctAnswer,
            elapsedTime = elapsedSeconds,
            timeLimit = timeLimit,
            onCorrect = {
                // resume shake detection after dialog
                gameHandler.postDelayed({ isShakingAllowed = true }, 1000)
                proceedToNextQuestion()
            },
            onIncorrect = {
                gameHandler.postDelayed({ isShakingAllowed = true }, 1000)
                resetShake()
            },
            onShowCorrect = {
                gameHandler.postDelayed({ isShakingAllowed = true }, 1000)
                proceedToNextQuestion()
            }
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isShakingAllowed || event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (acceleration > 12f) {
            onShakeDetected()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun onShakeDetected() {
        if (!isShakingAllowed) return

        isShakingAllowed = false
        shakeHandler.postDelayed({ isShakingAllowed = true }, 500)

        count++
        binding?.ringCount?.text = count.toString()
        tts.stop()
        announce(binding?.ringCount, count.toString())

        val question = parsedShakeList[index % parsedShakeList.size]

        if (count > question.answer && !answerChecked) {
            checkAnswer(forceWrong = true)
            return
        }

        if (firstShakeTime == 0L) {
            firstShakeTime = System.currentTimeMillis()
            answerChecked = false
        }

        if (count == question.answer && !answerChecked) {
            gameHandler.postDelayed({
                checkAnswer()
            }, 1500)
        }
    }
}
