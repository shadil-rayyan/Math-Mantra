package com.zendalona.mathsmanthra.ui

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.zendalona.mathsmanthra.R
import com.zendalona.mathsmanthra.databinding.DialogResultBinding
import com.zendalona.mathsmanthra.databinding.FragmentQuickPlayBinding
import com.zendalona.mathsmanthra.utility.settings.DifficultyPreferences
import java.io.IOException
import java.util.*
import kotlin.random.Random

class QuickPlayFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentQuickPlayBinding? = null
    private val binding get() = _binding!!

    private lateinit var tts: TextToSpeech
    private var questionList = mutableListOf<Pair<String, Int>>() // (displayed question, answer)
    private var rawQuestions = mutableListOf<String>()
    private val wrongQuestionsSet = mutableSetOf<Int>()  // track wrong questions by index
    private var currentQuestionAttempts = 0  // attempts for current question
    // Raw question strings to parse dynamically
    private var currentIndex = -1
    private var totalScore = 0
    private var totalQuestions = 0
    private lateinit var difficulty: String
    private var startTime: Long = 0
    private var currentQuestionTimeLimit = 60 // default seconds
    private var mediaPlayer: MediaPlayer? = null


    // Grading points map
    private val gradePoints = mapOf(
        "Excellent" to 50,
        "Very Good" to 40,
        "Good" to 30,
        "Fair" to 20,
        "Okay" to 10,
        "Wrong Answer" to -10
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(requireContext(), this)
        difficulty = DifficultyPreferences.getDifficulty(requireContext())
        loadRawQuestionsFromAssets()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickPlayBinding.inflate(inflater, container, false)

        binding.submitAnswerBtn.setOnClickListener { checkAnswer() }

        loadNextQuestion()
        return binding.root
    }

    private fun loadRawQuestionsFromAssets() {
        val filename = "quickplay/landingpage/quickplay_${difficulty.lowercase(Locale.ROOT)}.txt"
        Log.d("QuickPlay", "Loading questions from asset file: $filename")

        try {
            val inputStream = requireContext().assets.open(filename)
            val lines = inputStream.bufferedReader().readLines()

            rawQuestions.clear()

            for ((index, line) in lines.withIndex()) {
                if (line.isBlank()) continue

                val parts = line.split("===")
                if (parts.size < 3) {
                    Log.w("QuickPlay", "Skipping invalid line $index")
                    continue
                }

                rawQuestions.add(line.trim())
            }

            if (rawQuestions.isEmpty()) {
                Toast.makeText(requireContext(), "No valid questions found", Toast.LENGTH_LONG).show()
            }

        } catch (e: IOException) {
            Log.e("QuickPlay", "IOException while loading file: $filename", e)
            Toast.makeText(requireContext(), "Error reading file: $filename", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("QuickPlay", "Unexpected error while loading questions", e)
            Toast.makeText(requireContext(), "Error loading questions", Toast.LENGTH_LONG).show()
        }
    }

    // --- Parsing helpers ---

    private fun parseToken(token: String): Int {
        return when {
            ',' in token -> { // listing_symbol
                val options = token.split(",").mapNotNull { it.toIntOrNull() }
                if (options.isEmpty()) 0 else options.random()
            }
            ':' in token -> { // range_symbol
                val bounds = token.split(":").mapNotNull { it.toIntOrNull() }
                if (bounds.size == 2) {
                    val (start, end) = bounds
                    Random.nextInt(start, end + 1)
                } else 0
            }
            ';' in token -> { // multiplier_symbol
                val parts = token.split(";")
                if (parts.size == 3) {
                    val digit = parts[0].toIntOrNull() ?: 0
                    val start = parts[1].toIntOrNull() ?: 1
                    val end = parts[2].toIntOrNull() ?: 1
                    val multiplier = Random.nextInt(start, end + 1)
                    digit * multiplier
                } else 0
            }
            else -> token.toIntOrNull() ?: 0
        }
    }

    private fun evalSimpleExpression(expression: String): Int {
        // Assumes expression only contains digits and +,-,*,/
        val tokens = mutableListOf<String>()
        var current = ""
        for (ch in expression) {
            if (ch in setOf('+', '-', '*', '/')) {
                if (current.isNotEmpty()) tokens.add(current)
                tokens.add(ch.toString())
                current = ""
            } else {
                current += ch
            }
        }
        if (current.isNotEmpty()) tokens.add(current)

        var result = tokens[0].toIntOrNull() ?: 0
        var index = 1
        while (index < tokens.size) {
            val op = tokens[index]
            val num = tokens[index + 1].toIntOrNull() ?: 0
            when (op) {
                "+" -> result += num
                "-" -> result -= num
                "*" -> result *= num
                "/" -> if (num != 0) result /= num
            }
            index += 2
        }
        return result
    }

    private fun parseExpression(expr: String): Pair<String, Int> {
        val operators = setOf('+', '-', '*', '/')
        var i = 0
        val n = expr.length

        var currentNumberToken = ""
        val resultExpressionBuilder = StringBuilder()

        while (i < n) {
            val ch = expr[i]

            if (ch in operators) {
                val value = parseToken(currentNumberToken)
                resultExpressionBuilder.append(value).append(ch)
                currentNumberToken = ""
                i++
            } else {
                currentNumberToken += ch
                i++
            }
        }
        if (currentNumberToken.isNotEmpty()) {
            val value = parseToken(currentNumberToken)
            resultExpressionBuilder.append(value)
        }

        val resultExpression = resultExpressionBuilder.toString()
        val answer = evalSimpleExpression(resultExpression)

        return Pair(resultExpression, answer)
    }

    // --- Main game logic ---

    private fun loadNextQuestion() {
        currentIndex++
        currentQuestionAttempts = 0  // reset attempts for new question

        if (currentIndex >= rawQuestions.size) {
            endGame()
            return
        }

        val parts = rawQuestions[currentIndex].split("===")
        if (parts.size < 3) {
            Toast.makeText(requireContext(), "Invalid question format", Toast.LENGTH_SHORT).show()
            loadNextQuestion() // skip invalid
            return
        }

        val rawExpression = parts[0].trim()
        val timeLimit = parts[1].trim().toIntOrNull() ?: 20
        val soundFlag = parts[2].trim().toIntOrNull() ?: 0

        val (questionText, correctAnswer) = parseExpression(rawExpression)

        if (questionList.size > currentIndex) {
            questionList[currentIndex] = questionText to correctAnswer
        } else {
            questionList.add(questionText to correctAnswer)
        }

        binding.questionTv.text = questionText
        speakQuestion(questionText)
        binding.answerEt.text?.clear()
        startTime = System.currentTimeMillis()
        currentQuestionTimeLimit = timeLimit
        totalQuestions = rawQuestions.size

        Log.d("QuickPlay", "Question #$currentIndex: $questionText = $correctAnswer (Time limit: $timeLimit s, Sound flag: $soundFlag)")
    }


    private fun checkAnswer() {
        if (currentIndex >= questionList.size) return

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].second
        val elapsedTimeMs = System.currentTimeMillis() - startTime
        val elapsedSeconds = elapsedTimeMs / 1000.0

        if (userInput == correctAnswer) {
            val grade = getGrade(elapsedSeconds, currentQuestionTimeLimit.toDouble(), true)
            val points = gradePoints[grade] ?: 0
            totalScore += points
            playSound("correct")

            showResultDialog(grade, true) {
                loadNextQuestion()
            }

        } else {
            playSound("wrong")
            currentQuestionAttempts++

            if (currentQuestionAttempts < 3) {
                val message = "Wrong! Try again. Attempt $currentQuestionAttempts of 3."
                showCustomRetryDialog(message) {
                    binding.answerEt.text?.clear()
                }
            } else {
                // 3 wrong attempts
                val points = gradePoints["Wrong Answer"] ?: 0
                totalScore += points
                wrongQuestionsSet.add(currentIndex)

                val message = "Wrong! The correct answer is $correctAnswer"

                showCustomRetryDialog(message) {
                    if (wrongQuestionsSet.size >= 4) {
                        endGame()
                    } else {
                        loadNextQuestion()
                    }
                }
            }
        }
    }


    private fun getGrade(elapsedTime: Double, allottedTime: Double, isCorrect: Boolean): String {
        if (!isCorrect) return "Wrong Answer"

        return when {
            elapsedTime <= allottedTime * 0.5 -> "Excellent"
            elapsedTime <= allottedTime * 0.75 -> "Very Good"
            elapsedTime <= allottedTime -> "Good"
            elapsedTime <= allottedTime * 1.25 -> "Fair"
            else -> "Okay"
        }
    }

    private fun playSound(name: String) {
        val resId = when (name) {
            "correct" -> R.raw.correct_sound
            "wrong" -> R.raw.wrong_sound
            else -> null
        }

        resId?.let {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(requireContext(), it)
            mediaPlayer?.start()
        }
    }

    private fun speakQuestion(question: String) {
        val spokenText = question.replace("+", " plus ")
            .replace("-", " minus ")
            .replace("*", " multiplied by ")
            .replace("/", " divided by ")
            .replace("%", " percentage of ")
            .replace(",", " and ")

        tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showResultDialog(grade: String, isCorrect: Boolean, onContinue: () -> Unit) {
        val message = when (grade) {
            "Excellent", "Very Good", "Good", "Fair", "Okay" -> "$grade Answer"
            else -> "Wrong Answer"
        }
        val gifResource = when (grade) {
            "Excellent" -> R.drawable.right
            "Very Good" -> R.drawable.right
            "Good" -> R.drawable.right
            "Fair" -> R.drawable.right
            "Okay" -> R.drawable.right
            else -> R.drawable.wrong
        }

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)

        val inflater = layoutInflater
        val dialogBinding = DialogResultBinding.inflate(inflater)
        val dialogView = dialogBinding.root

        Glide.with(this)
            .asGif()
            .load(gifResource)
            .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = message

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                onContinue()
            }
            .create()
            .show()
    }

    private fun showCustomRetryDialog(message: String, onContinue: () -> Unit) {
        val inflater = layoutInflater
        val dialogBinding = DialogResultBinding.inflate(inflater)
        val dialogView = dialogBinding.root

        val gifResource = if (message.startsWith("Wrong! The correct answer")) {
            R.drawable.wrong // show wrong answer gif
        } else {
            R.drawable.wrong // Add a new GIF for retry feedback
        }

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)

        Glide.with(this)
            .asGif()
            .load(gifResource)
            .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = message

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                onContinue()
            }
            .create()
            .show()
    }

    private fun endGame() {
        val maxPossibleScore = 50 * totalQuestions
        val finalPercentage = if (maxPossibleScore > 0) {
            (totalScore.toDouble() * 100) / maxPossibleScore
        } else 0.0

        Toast.makeText(requireContext(), "Quiz Over! Score: $totalScore, Grade: ${"%.2f".format(finalPercentage)}%", Toast.LENGTH_LONG).show()
        speakQuestion("Quiz over! Your final score is $totalScore")

        // Pass data to ResultFragment
        val bundle = Bundle().apply {
            putInt("score", totalScore)
            putInt("totalQuestions", totalQuestions)
            putDouble("percentage", finalPercentage)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EndScoreFragment::class.java, bundle)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }
}
