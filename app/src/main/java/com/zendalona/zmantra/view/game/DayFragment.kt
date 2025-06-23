package com.zendalona.zmantra.view.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameDayBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.utility.common.DialogUtils
import com.zendalona.zmantra.utility.common.EndScore.endGameWithScore
import com.zendalona.zmantra.utility.common.GradingUtils
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import kotlin.random.Random

class DayFragment : Fragment(), Hintable {

    private var _binding: FragmentGameDayBinding? = null
    private val binding get() = _binding!!

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private var correctDay = ""
    private var attemptCount = 0
    private var totalWrongQuestions = 0

    private lateinit var ttsUtility: TTSUtility
    private var questionStartTime: Long = 0L
    private val totalTime: Double = 30.0 // seconds

    private var dayQuestions: List<GameQuestion> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameDayBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ttsUtility = TTSUtility(requireContext())

        // Load Excel day questions once
        dayQuestions = ExcelQuestionLoader.loadQuestionsFromExcel(
            requireContext(),
            lang = "en",       // Change to dynamic locale if needed
            mode = "day",
            difficulty = "1"
        )

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
        if (dayQuestions.isEmpty()) {
            binding.questionText.text = getString(R.string.no_questions_available)
            return
        }

        val randomQuestion = dayQuestions.random()
        val operand = randomQuestion.answer // This is the number of days to add/subtract

        val startDayIndex = Random.nextInt(days.size)
        val startDay = days[startDayIndex]

        val correctIndex = (startDayIndex + operand) % 7
        correctDay = days[correctIndex]
        attemptCount = 0
        questionStartTime = System.currentTimeMillis()

        val startDayLocalized = getString(getDayStringRes(startDay))

        // Use a specific string for this style of question
        val questionText = getString(
            R.string.question_day_offset_template,
            startDayLocalized,
            operand
        )

        binding.questionText.text = questionText
        enableAllButtons()
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
            putString("mode", "day")
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
