package com.zendalona.zmantra.presentation.features.game.compass

import android.content.Context
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
import android.widget.Toast
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.databinding.FragmentGameCompassBinding
import com.zendalona.zmantra.domain.model.GameQuestion
import com.zendalona.zmantra.presentation.features.game.compass.util.CompassUtils

class CompassFragment : BaseGameFragment(), SensorEventListener {

    private var binding: FragmentGameCompassBinding? = null
    private var sensorManager: SensorManager? = null
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var isFirstQuestion = true
    private var targetDirection = 0f
    private var questionAnswered = false
    private var questionStartTime: Long = 0L

    private val questions: MutableList<GameQuestion> = mutableListOf()
    private var currentIndex = -1
    private var currentAzimuth = 0f
    private lateinit var compassDirections: Array<String>

    private val directionAnnounceHandler = Handler(Looper.getMainLooper())
    private val directionAnnounceRunnable = object : Runnable {
        override fun run() {
            announceCurrentDirection()
            directionAnnounceHandler.postDelayed(this, 10000)
        }
    }

    private val holdHandler = Handler(Looper.getMainLooper())
    private val holdRunnable = Runnable {
        questionAnswered = true
        val elapsed = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val grade = getGrade(elapsed, questions[currentIndex].timeLimit.toDouble())
        showResultDialog(grade) { generateNewQuestion() }
    }

    override fun getModeName(): String = "direction"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameCompassBinding.inflate(inflater, container, false)
        compassDirections = requireContext().resources.getStringArray(R.array.compass_directions)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rootLayout?.postDelayed({
            announce(binding?.rootLayout, getString(R.string.compass_turn_to, ""))
        }, 500)
    }

    override fun onQuestionsLoaded(loaded: List<GameQuestion>) {
        if (loaded.isEmpty()) {
            Toast.makeText(requireContext(), "No compass questions found", Toast.LENGTH_LONG).show()
            endGame()
        } else {
            questions.clear()
            questions.addAll(loaded)
            generateNewQuestion()
        }
    }

    private fun generateNewQuestion() {
        if (++currentIndex >= questions.size) {
            endGame()
            return
        }

        val q = questions[currentIndex]
        targetDirection = CompassUtils.directionToDegrees(q.expression, compassDirections)
        questionAnswered = false
        questionStartTime = System.currentTimeMillis()
        holdHandler.removeCallbacks(holdRunnable)

        val questionText = getString(R.string.compass_turn_to, q.expression)
        binding?.questionTv?.text = questionText
        announce(binding?.questionTv, questionText)

        if (isFirstQuestion) {
            binding?.questionTv?.requestFocus()
            isFirstQuestion = false
        }

        directionAnnounceHandler.removeCallbacks(directionAnnounceRunnable)
        directionAnnounceHandler.postDelayed(directionAnnounceRunnable, 5000)
    }

    private fun updateCompassUI(rotationDegrees: Float, actualAzimuth: Float) {
        binding?.compass?.rotation = rotationDegrees
        currentAzimuth = actualAzimuth
        binding?.degreeText?.text =
            CompassUtils.getCompassDirection(actualAzimuth, compassDirections)
        checkIfHoldingCorrectDirection(actualAzimuth)
    }

    private fun checkIfHoldingCorrectDirection(currentDegrees: Float) {
        if (questionAnswered) return

        val diff = CompassUtils.angleDifference(targetDirection, currentDegrees)

        if (diff <= 22.5f) {
            if (!holdHandler.hasCallbacks(holdRunnable)) {
                holdHandler.postDelayed(holdRunnable, 3000)
            }
        } else {
            holdHandler.removeCallbacks(holdRunnable)
        }
    }

    private fun announceCurrentDirection() {
        val directionText = CompassUtils.getCompassDirection(currentAzimuth, compassDirections)
        val message = getString(R.string.compass_current_direction, directionText)
        announce(binding?.root, message)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                lastAccelerometerSet = true
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                lastMagnetometerSet = true
            }
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
            SensorManager.getOrientation(rotationMatrix, orientation)

            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val azimuthFixed = (azimuth + 360) % 360

            updateCompassUI(-azimuthFixed, azimuthFixed)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        directionAnnounceHandler.postDelayed(directionAnnounceRunnable, 5000)
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        directionAnnounceHandler.removeCallbacks(directionAnnounceRunnable)
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        holdHandler.removeCallbacks(holdRunnable)
    }
}
