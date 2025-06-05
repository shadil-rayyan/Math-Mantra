package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameCompassBinding
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences.getDifficulty
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.abs

class CompassFragment : Fragment(), SensorEventListener {
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

    private val rawQuestions: MutableList<String> = ArrayList<String>()
    private var currentIndex = -1
    private var currentTargetDirection = "North"
    private var currentTimeLimit = 30

    private lateinit var compassDirections: Array<String>


    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if (sensorManager != null) {
            magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameCompassBinding.inflate(inflater, container, false)
        compassDirections = requireContext().resources.getStringArray(R.array.compass_directions)

        loadQuestionsFromAssets()
        generateNewQuestion() // start first
        return binding!!.getRoot()
    }

    private fun loadQuestionsFromAssets() {
        rawQuestions.clear()
        val difficulty = getDifficulty(requireContext()).lowercase(Locale.getDefault())
        val lang = LocaleHelper.getLanguage(context) ?: "en" // fallback to English

        val fileName = "$lang/game/compass/$difficulty.txt"
        Log.d("Compass", "Language: $lang, File: $fileName")

        try {
            val reader = BufferedReader(
                InputStreamReader(requireContext().assets.open(fileName))
            )
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.trim().startsWith("compass?")) {
                    rawQuestions.add(line!!.trim())
                }
            }
            reader.close()

            Log.d("Compass", "Questions loaded: ${rawQuestions.size}")

            if (rawQuestions.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "No valid questions found in $fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: IOException) {
            Toast.makeText(
                requireContext(),
                "Failed to load questions: $fileName",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun generateNewQuestion() {
        currentIndex++
        if (currentIndex >= rawQuestions.size) {
            endGame()
            return
        }

        val raw = rawQuestions.get(currentIndex)
        val parts: Array<String?> =
            raw.replace("compass?", "").split("==".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()

        if (parts.size >= 1) currentTargetDirection = parts[0]!!.trim { it <= ' ' }
        if (parts.size >= 2) currentTimeLimit = parts[1]!!.trim { it <= ' ' }.toInt()

        targetDirection = directionToDegrees(currentTargetDirection)
        questionAnswered = false

        val questionText = getString(R.string.compass_turn_to, currentTargetDirection)
        binding!!.questionTv.setText(questionText)
        binding!!.questionTv.announceForAccessibility(questionText)
    }

    private fun directionToDegrees(dir: String): Float {
        when (dir.lowercase(Locale.getDefault())) {
            "north" -> return 0f
            "northeast" -> return 45f
            "east" -> return 90f
            "southeast" -> return 135f
            "south" -> return 180f
            "southwest" -> return 225f
            "west" -> return 270f
            "northwest" -> return 315f
            else -> return 0f
        }
    }

    override fun onResume() {
        super.onResume()
        if (sensorManager != null) {
            sensorManager!!.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        if (sensorManager != null) {
            sensorManager!!.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
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

            updateCompassUI(-azimuthFixed)
        }
    }

    private fun updateCompassUI(rotationDegrees: Float) {
        if (binding == null) return

        binding!!.compass.setRotation(rotationDegrees)

        val directionText = getCompassDirection((rotationDegrees + 360) % 360)
        binding!!.degreeText.setText(directionText)

        val questionText = getString(R.string.compass_turn_to, currentTargetDirection)
        Log.d("CompassDebug", "Target direction (raw): $currentTargetDirection")
        Log.d("CompassDebug", "Localized question text: $questionText")
        binding!!.questionTv.text = questionText
        binding!!.questionTv.announceForAccessibility(questionText)


        checkIfCorrect(rotationDegrees)
    }

    private fun checkIfCorrect(currentDegrees: Float) {
        if (questionAnswered) return

        var diff = abs((targetDirection - ((currentDegrees + 360) % 360)).toDouble()).toFloat()
        if (diff > 180) diff = 360 - diff

        if (diff <= 22.5) {
            questionAnswered = true
            showResultDialog()
        }
    }

    private fun showResultDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.compass_correct))
            .setMessage(getString(R.string.compass_correct_msg))
            .setCancelable(false)
            .create()

        dialog.show()
        binding!!.getRoot().announceForAccessibility(getString(R.string.compass_correct_msg))

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if (dialog.isShowing()) {
                dialog.dismiss()
                generateNewQuestion()
            }
        }, 3000)
    }

    private fun endGame() {
        Toast.makeText(requireContext(), "Game Over!", Toast.LENGTH_LONG).show()
        binding!!.questionTv.setText("Game Over")
    }

    private fun getCompassDirection(degrees: Float): String? {
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return compassDirections[index]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
