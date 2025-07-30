package com.zendalona.zmantra.view.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentGameDayBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.view.base.BaseGameFragment
import kotlin.random.Random

class DayFragment : BaseGameFragment() {

    private var _binding: FragmentGameDayBinding? = null
    private val binding get() = _binding!!

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private lateinit var buttons: List<Button>

    private var correctDay = ""
    private var questionStartTime: Long = 0L
    private val totalTime: Double = 30.0 // seconds

    private var questions: List<GameQuestion> = emptyList()

    override fun getModeName(): String = "day"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameDayBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        buttons = listOf(
            binding.btnMonday, binding.btnTuesday, binding.btnWednesday,
            binding.btnThursday, binding.btnFriday, binding.btnSaturday, binding.btnSunday
        )

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttons = listOf(
            binding.btnMonday, binding.btnTuesday, binding.btnWednesday,
            binding.btnThursday, binding.btnFriday, binding.btnSaturday, binding.btnSunday
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                checkAnswer(button.text.toString())
            }
        }
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = questions
        if (questions.isEmpty()) {
            binding.questionText.text = getString(R.string.no_questions_available)
        } else {
            generateQuestion()
        }
    }

    private fun generateQuestion() {
        val randomQuestion = questions.random()
        val operand = randomQuestion.answer

        val startDayIndex = Random.nextInt(days.size)
        val startDay = days[startDayIndex]

        val correctIndex = (startDayIndex + operand) % 7
        correctDay = days[correctIndex]
        attemptCount = 0
        questionStartTime = System.currentTimeMillis()

        val startDayLocalized = getString(getDayStringRes(startDay))
        val questionText = getString(R.string.question_day_offset_template, startDayLocalized, operand)

        binding.questionText.text = questionText
        announce(binding.questionText, questionText)
        enableAllButtons()
    }

    private fun checkAnswer(selected: String) {
        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val correctDayLocalized = getString(getDayStringRes(correctDay))

        if (selected == correctDayLocalized) {
            handleAnswerSubmission(
                userAnswer = selected,
                correctAnswer = correctDayLocalized,
                elapsedTime = elapsedTime,
                timeLimit = totalTime,
                onCorrect = {
                    disableAllButtons()
                    generateQuestion()

                },
                onIncorrect = {},
                onShowCorrect = {}
            )
        } else {
            attemptCount++
            if (attemptCount >= 3) {
                handleAnswerSubmission(
                    userAnswer = selected,
                    correctAnswer = correctDayLocalized,
                    elapsedTime = elapsedTime,
                    timeLimit = totalTime,
                    onCorrect = {},
                    onIncorrect = {},
                    onShowCorrect = {
                        disableAllButtons()
                        generateQuestion()

                    }
                )
            } else {
                handleAnswerSubmission(
                    userAnswer = selected,
                    correctAnswer = correctDayLocalized,
                    elapsedTime = elapsedTime,
                    timeLimit = totalTime,
                    onCorrect = {},
                    onIncorrect = {},
                    onShowCorrect = {}
                )
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

    private fun disableAllButtons() {
        buttons.forEach { it.isEnabled = false }
    }

    private fun enableAllButtons() {
        buttons.forEach { it.isEnabled = true }
    }
}
