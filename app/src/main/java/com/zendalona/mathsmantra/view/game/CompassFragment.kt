package com.zendalona.mathsmantra.view.game

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
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameCompassBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.view.HintFragment
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences.getDifficulty
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
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

    private val rawQuestions: MutableList<String> = ArrayList()
    private var currentIndex = -1
    private var currentTargetDirection = "North"
    private var currentTimeLimit = 30

    private lateinit var compassDirections: Array<String>

    private var currentAzimuth = 0f

    private val holdHandler = Handler(Looper.getMainLooper())
    private val holdRunnable = Runnable {
        questionAnswered = true
        showResultDialog()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        sensorManager?.let {
            magnetometer = it.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            accelerometer = it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameCompassBinding.inflate(inflater, container, false)
        compassDirections = requireContext().resources.getStringArray(R.array.compass_directions)
        setHasOptionsMenu(true)
        loadQuestionsFromAssets()
        generateNewQuestion()

        return binding!!.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true  // Show hint here
    }

    private fun loadQuestionsFromAssets() {
        rawQuestions.clear()
        val difficulty = getDifficulty(requireContext()).lowercase(Locale.getDefault())
        val lang = LocaleHelper.getLanguage(context) ?: "en"

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

        val raw = rawQuestions[currentIndex]
        val parts = raw.replace("compass?", "")
            .split("===".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        if (parts.isNotEmpty()) currentTargetDirection = parts[0]!!.trim()
        if (parts.size >= 2) currentTimeLimit = parts[1]!!.trim().toInt()

        targetDirection = directionToDegrees(currentTargetDirection)
        questionAnswered = false

        holdHandler.removeCallbacks(holdRunnable)

        val questionText = getString(R.string.compass_turn_to, currentTargetDirection)
        binding!!.questionTv.text = questionText
        binding!!.questionTv.announceForAccessibility(questionText)
    }

    private fun directionToDegrees(dir: String): Float {
        return when (dir.lowercase(Locale.getDefault())) {
            "north" -> 0f
            "northeast" -> 45f
            "east" -> 90f
            "southeast" -> 135f
            "south" -> 180f
            "southwest" -> 225f
            "west" -> 270f
            "northwest" -> 315f
            else -> 0f
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        holdHandler.removeCallbacks(holdRunnable)
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

    private fun updateCompassUI(rotationDegrees: Float, actualAzimuth: Float) {
        if (binding == null) return

        binding!!.compass.rotation = rotationDegrees

        currentAzimuth = actualAzimuth
        val directionText = getCompassDirection(actualAzimuth)
        binding!!.degreeText.text = directionText

        val questionText = getString(R.string.compass_turn_to, currentTargetDirection)
        binding!!.questionTv.text = questionText
//        binding!!.questionTv.announceForAccessibility(questionText)

        checkIfHoldingCorrectDirection(actualAzimuth)
    }

    private fun checkIfHoldingCorrectDirection(currentDegrees: Float) {
        if (questionAnswered) return

        var diff = abs((targetDirection - ((currentDegrees + 360) % 360)).toDouble()).toFloat()
        if (diff > 180) diff = 360 - diff

        if (diff <= 22.5) {
            if (!holdHandler.hasCallbacks(holdRunnable)) {
                holdHandler.postDelayed(holdRunnable, 3000)
            }
        } else {
            holdHandler.removeCallbacks(holdRunnable)
        }
    }

    private fun showResultDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.compass_correct))
            .setMessage(getString(R.string.compass_correct_msg))
            .setCancelable(false)
            .create()

        dialog.show()
        binding!!.root.announceForAccessibility(getString(R.string.compass_correct_msg))

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                generateNewQuestion()
            }
        }, 3000)
    }

    private fun endGame() {
        Toast.makeText(requireContext(), "Game Over!", Toast.LENGTH_LONG).show()
        binding!!.questionTv.text = "Game Over"
    }

    private fun getCompassDirection(degrees: Float): String? {
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return compassDirections[index]
    }

    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "compass") // Pass only the mode
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
