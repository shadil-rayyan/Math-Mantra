package com.zendalona.zmantra.view.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameCompassBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper
import com.zendalona.zmantra.view.HintFragment
import kotlinx.coroutines.launch
import kotlin.math.abs

class CompassFragment : Fragment(), SensorEventListener, Hintable {

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

    private var targetDirection = 0f
    private var questionAnswered = false
    private var questionStartTime: Long = 0L

    private val rawQuestions: MutableList<String> = ArrayList()
    private var currentIndex = -1
    private var currentTargetDirection = "North"
    private var currentTimeLimit = 30
    private var isFirstOpen = true
    private lateinit var ttsUtility: TTSUtility
    private lateinit var compassDirections: Array<String>
    private var currentAzimuth = 0f

    // Handler and Runnable for announcing the direction every 4 seconds
    private val directionAnnounceHandler = Handler(Looper.getMainLooper())
    private val directionAnnounceRunnable = object : Runnable {
        override fun run() {
            // Announce the current compass direction for accessibility
            announceCurrentDirection()

            // Repeat every 4 seconds
            directionAnnounceHandler.postDelayed(this, 4000)
        }
    }

    private val holdHandler = Handler(Looper.getMainLooper())
    private val holdRunnable = Runnable {
        questionAnswered = true
        val elapsedSeconds = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val grade = GradingUtils.getGrade(elapsedSeconds, currentTimeLimit.toDouble(), true)
        DialogUtils.showResultDialog(requireContext(), layoutInflater, ttsUtility, grade) {
            generateNewQuestion()
        }
    }

    private var questionsLoaded = false // Boolean flag to track if questions are loaded

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        ttsUtility = TTSUtility(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameCompassBinding.inflate(inflater, container, false)
        compassDirections = requireContext().resources.getStringArray(R.array.compass_directions)
        setHasOptionsMenu(true)

        loadQuestionsFromExcel() // Start loading questions asynchronously
        return binding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Wait for questions to be loaded before generating the first question
        if (questionsLoaded) {
            generateNewQuestion()
        }

        if (isFirstOpen) {
            binding?.rootLayout?.apply {
                requestFocus()
                postDelayed({
                    announceForAccessibility(contentDescription)
                }, 500)
            }
            isFirstOpen = false
        }
    }

    private fun loadQuestionsFromExcel() {
        val difficultyNum = DifficultyPreferences.getDifficulty(requireContext())
        var difficulty = difficultyNum.toString()
        val lang = LocaleHelper.getLanguage(context) ?: "en"

        lifecycleScope.launch {
            // Load the questions asynchronously
            val questions =  ExcelQuestionLoader.loadQuestionsFromExcel(
                requireContext(), lang, "compass", difficulty
            )

            if (questions.isEmpty()) {
                Toast.makeText(requireContext(), "No compass questions found", Toast.LENGTH_LONG).show()
            } else {
                rawQuestions.clear()
                questions.forEach { q ->
                    rawQuestions.add("${q.expression}===${q.timeLimit}")
                }
            }

            // Set the flag to true once questions are loaded
            questionsLoaded = true

            // Now, generate a new question (if necessary)
            if (questionsLoaded) {
                generateNewQuestion()
            }
        }
    }

    private fun generateNewQuestion() {
        if (rawQuestions.isEmpty()) {
            endGame()
            return
        }

        questionStartTime = System.currentTimeMillis()
        currentIndex++
        if (currentIndex >= rawQuestions.size) {
            endGame()
            return
        }

        val parts = rawQuestions[currentIndex].split("===")
        if (parts.isNotEmpty()) currentTargetDirection = parts[0].trim()
        if (parts.size >= 2) currentTimeLimit = parts[1].trim().toInt()

        targetDirection = directionToDegrees(currentTargetDirection)
        questionAnswered = false

        holdHandler.removeCallbacks(holdRunnable)

        val questionText = getString(R.string.compass_turn_to, currentTargetDirection)
        binding!!.questionTv.text = questionText
    }

    private fun directionToDegrees(dir: String): Float {
        val directions = resources.getStringArray(R.array.compass_directions)
        val index = directions.indexOfFirst { it.equals(dir, ignoreCase = true) }
        return if (index != -1) index * 22.5f else 0f
    }

    override fun onResume() {
        super.onResume()

        // Start announcing the direction every 4 seconds
        directionAnnounceHandler.postDelayed(directionAnnounceRunnable, 4000)

        // Register the sensors
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()

        // Stop the repeated direction announcements
        directionAnnounceHandler.removeCallbacks(directionAnnounceRunnable)

        // Unregister the sensors
        sensorManager?.unregisterListener(this)
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
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            SensorManager.getOrientation(rotationMatrix, orientation)

            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val azimuthFixed = (azimuth + 360) % 360

            updateCompassUI(-azimuthFixed, azimuthFixed)
        }
    }

    private fun updateCompassUI(rotationDegrees: Float, actualAzimuth: Float) {
        binding?.compass?.rotation = rotationDegrees
        currentAzimuth = actualAzimuth
        val directionText = getCompassDirection(actualAzimuth)
        binding?.degreeText?.text = directionText

        // Announce the current direction
        announceCurrentDirection()

        val questionText = getString(R.string.compass_turn_to, currentTargetDirection)
        binding?.questionTv?.text = questionText

        checkIfHoldingCorrectDirection(actualAzimuth)
    }

    private fun checkIfHoldingCorrectDirection(currentDegrees: Float) {
        if (questionAnswered) return

        val normalizedTarget = (targetDirection + 360) % 360
        val normalizedCurrent = (currentDegrees + 360) % 360

        var diff = abs(normalizedTarget - normalizedCurrent)
        if (diff > 180) diff = 360 - diff

        if (diff <= 22.5f) {
            if (!holdHandler.hasCallbacks(holdRunnable)) {
                holdHandler.postDelayed(holdRunnable, 3000)
            }
        } else {
            holdHandler.removeCallbacks(holdRunnable)
        }
    }

    private fun endGame() {
        Toast.makeText(requireContext(), "Game Over!", Toast.LENGTH_LONG).show()
        binding?.questionTv?.text = "Game Over"
    }

    private fun getCompassDirection(degrees: Float): String {
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return compassDirections[index]
    }

    private fun announceCurrentDirection() {
        // Get the direction text (for TalkBack)
        val directionText = getCompassDirection(currentAzimuth)

        // Announce the direction for accessibility
        binding?.root?.announceForAccessibility("Turn towards $directionText")
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "compass")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        holdHandler.removeCallbacks(holdRunnable)
    }
}
