package com.zendalona.mathsmantra.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.databinding.FragmentGameDayBinding
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.common.GradingUtils
import com.zendalona.mathsmantra.utility.common.TTSUtility
import kotlin.random.Random

class DayFragment : Fragment() {

    private var _binding: FragmentGameDayBinding? = null
    private val binding get() = _binding!!

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private var startDayIndex = 0
    private var addDays = 0
    private var correctDay = ""

    private var attemptCount = 0
    private var totalWrongQuestions = 0

    private lateinit var ttsUtility: TTSUtility
    private var questionStartTime: Long = 0L
    private val totalTime: Double = 30.0 // in seconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ttsUtility = TTSUtility(requireContext())

        val buttons = listOf(
            binding.btnMonday, binding.btnTuesday, binding.btnWednesday,
            binding.btnThursday, binding.btnFriday, binding.btnSaturday, binding.btnSunday
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                checkAnswer(button.text.toString(), buttons)
            }
        }

        generateQuestion()
    }

    private fun generateQuestion() {
        startDayIndex = Random.nextInt(days.size)
        addDays = Random.nextInt(1, 14) // 1 to 13 days
        correctDay = days[(startDayIndex + addDays) % 7]
        attemptCount = 0

        val startDay = days[startDayIndex]
        binding.questionText.text = "If today is $startDay,\nwhat day is after $addDays days?"

        enableAllButtons()
        questionStartTime = System.currentTimeMillis()
    }

    private fun checkAnswer(selected: String, buttons: List<Button>) {
        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0 // in seconds

        if (selected == correctDay) {
            val grade = GradingUtils.getGrade(elapsedTime, totalTime, isCorrect = true)
            disableAllButtons(buttons)

            DialogUtils.showResultDialog(
                requireContext(),
                layoutInflater,
                ttsUtility,
                grade
            ) {
                generateQuestion()
            }

        } else {
            attemptCount++
            ttsUtility.speak("Wrong answer. Try again.")

            if (attemptCount >= 3) {
                totalWrongQuestions++
                disableAllButtons(buttons)

                if (totalWrongQuestions >= 3) {
                    // End game after 3 wrong questions
                    this.endGameWithScore()
                } else {
                    // Show correct answer, then go to next question
                    DialogUtils.showRetryDialog(
                        requireContext(),
                        layoutInflater,
                        ttsUtility,
                        "Wrong! The correct answer was $correctDay"
                    ) {
                        generateQuestion()
                    }
                }
            }
        }
    }

    private fun disableAllButtons(buttons: List<Button>) {
        buttons.forEach { it.isEnabled = false }
    }

    private fun enableAllButtons() {
        listOf(
            binding.btnMonday, binding.btnTuesday, binding.btnWednesday,
            binding.btnThursday, binding.btnFriday, binding.btnSaturday, binding.btnSunday
        ).forEach { it.isEnabled = true }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ttsUtility.shutdown()
    }
}
