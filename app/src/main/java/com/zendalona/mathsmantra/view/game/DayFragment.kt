package com.zendalona.mathsmantra.view.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentGameDayBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.view.HintFragment
import com.zendalona.mathsmantra.utility.common.DialogUtils
import com.zendalona.mathsmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.mathsmantra.utility.common.GradingUtils
import com.zendalona.mathsmantra.utility.common.TTSUtility
import kotlin.random.Random

class DayFragment : Fragment(), Hintable {

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
    private val totalTime: Double = 30.0 // seconds


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameDayBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)  // Tell system this Fragment wants menu callbacks
        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true  // Show hint here
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

    private fun getDayStringRes(day: String): Int {
        return when (day) {
            "Monday" -> R.string.monday
            "Tuesday" -> R.string.tuesday
            "Wednesday" -> R.string.wednesday
            "Thursday" -> R.string.thursday
            "Friday" -> R.string.friday
            "Saturday" -> R.string.saturday
            "Sunday" -> R.string.sunday
            else -> throw IllegalArgumentException("Unknown day: $day")
        }
    }

    private fun generateQuestion() {
        startDayIndex = Random.nextInt(days.size)
        addDays = Random.nextInt(1, 14)
        correctDay = days[(startDayIndex + addDays) % 7]
        attemptCount = 0

        val startDay = days[startDayIndex]
        val startDayLocalized = getString(getDayStringRes(startDay))

        binding.questionText.text = getString(R.string.question_text_template, startDayLocalized, addDays)

        enableAllButtons()
        questionStartTime = System.currentTimeMillis()
    }

    private fun checkAnswer(selected: String, buttons: List<Button>) {
        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0

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
            ttsUtility.speak(getString(R.string.wrong_answer))

            if (attemptCount >= 3) {
                totalWrongQuestions++
                disableAllButtons(buttons)

                if (totalWrongQuestions >= 3) {
                    this.endGameWithScore()
                } else {
                    val correctDayLocalized = getString(getDayStringRes(correctDay))

                    DialogUtils.showRetryDialog(
                        requireContext(),
                        layoutInflater,
                        ttsUtility,
                        getString(R.string.wrong_answer_reveal, correctDayLocalized)
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
    override fun showHint() {
        val bundle = Bundle().apply {
            putString("mode", "day") // Pass only the mode
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ttsUtility.shutdown()
    }
}
