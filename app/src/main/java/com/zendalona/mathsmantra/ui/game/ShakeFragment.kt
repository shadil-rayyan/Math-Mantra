package com.zendalona.mathsmantra.ui.game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameShakeBinding
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.AccelerometerUtility
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import com.zendalona.mathsmantra.utility.common.*
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class ShakeFragment : Fragment() {

    private var binding: FragmentGameShakeBinding? = null
    private lateinit var accelerometerUtility: AccelerometerUtility
    private lateinit var tts: TTSUtility
    private var count = 0
    private var target = 0
    private var isShakingAllowed = true
    private val shakeHandler = Handler()
    private val gameHandler = Handler(Looper.getMainLooper())
    private var targetList: List<String> = emptyList()
    private var index = 0
    private lateinit var lang: String
    private lateinit var difficulty: String
    private var parsedShakeList: List<Pair<String, Int>> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = LocaleHelper.getLanguage(requireContext())
        difficulty = DifficultyPreferences.getDifficulty(requireContext())

        tts = TTSUtility(requireContext())
        accelerometerUtility = AccelerometerUtility(requireContext())
        parsedShakeList = loadShakeQuestionsFromAssets(lang, difficulty)
        if (parsedShakeList.isEmpty()) {
            parsedShakeList = listOf("2+1" to 3, "1+2+3" to 6)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameShakeBinding.inflate(inflater, container, false)
        startGame()
        return binding!!.root
    }

    private fun loadShakeQuestionsFromAssets(lang: String, difficulty: String): List<Pair<String, Int>> {
        val parsedQuestions = mutableListOf<Pair<String, Int>>()
        val fileName = "$lang/game/shake/${difficulty.lowercase(Locale.ROOT)}.txt"

        try {
            val reader = BufferedReader(InputStreamReader(requireContext().assets.open(fileName)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    val parsed = QuestionParser.parseExpression(it)
                    parsedQuestions.add(parsed)
                }
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Error loading shake questions: $fileName", Toast.LENGTH_SHORT).show()
        }
        return parsedQuestions
    }


    private fun startGame() {
        count = 0
        binding?.ringCount?.text = getString(R.string.shake_count_initial)

        val (question, answer) = parsedShakeList[index % parsedShakeList.size]
        target = answer

        val instruction = getString(R.string.shake_target_instruction, target)
        binding?.ringMeTv?.apply {
            text = instruction
            contentDescription = instruction
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            isFocusable = true
            isFocusableInTouchMode = true
            postDelayed({ requestFocus(); announceForAccessibility(instruction) }, 500)
        }

        tts.speak(instruction)
    }


    private fun onShakeDetected() {
        if (!isShakingAllowed) return

        isShakingAllowed = false
        shakeHandler.postDelayed({ isShakingAllowed = true }, 500)

        count++
        binding?.ringCount?.text = count.toString()

        VibrationUtils.vibrate(requireContext(), 100)
        tts.stop()
        val countText = getString(R.string.shake_count_announcement, count)
        tts.speak(countText)

        if (count == target) {
            evaluateGameResult()
        }
    }

    private fun evaluateGameResult() {
        gameHandler.postDelayed({
            if (count == target) showSuccessDialog()
            else showFailureDialog()
        }, 2000)
    }

    private fun showSuccessDialog() {
        val grade = GradingUtils.getGrade(0.0, 1.0, true)
        DialogUtils.showResultDialog(requireContext(), layoutInflater, tts, grade) {
            nextOrEnd()
        }
    }

    private fun showFailureDialog() {
        DialogUtils.showRetryDialog(
            requireContext(),
            layoutInflater,
            tts,
            getString(R.string.shake_failure)
        ) {
            nextOrEnd()
        }
    }

    private fun nextOrEnd() {
        index++
        if (index >= parsedShakeList.size) {
            tts.speak(getString(R.string.shake_game_over))
            endGameWithScore()
        } else {
            tts.speak(getString(R.string.shake_next_question))
            gameHandler.postDelayed({ startGame() }, 1000)
        }
    }


    fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/shake.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        accelerometerUtility.registerListener()
        isShakingAllowed = true
        shakeHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isVisible && accelerometerUtility.isDeviceShaken()) {
                    requireActivity().runOnUiThread { onShakeDetected() }
                }
                shakeHandler.postDelayed(this, 500)
            }
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        accelerometerUtility.unregisterListener()
        shakeHandler.removeCallbacksAndMessages(null)
        gameHandler.removeCallbacksAndMessages(null)
        tts.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        tts.shutdown()
    }
}
