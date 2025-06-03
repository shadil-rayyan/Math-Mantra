package com.zendalona.mathsmanthra.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zendalona.mathsmanthra.R
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
    private var rawQuestions = mutableListOf<String>() // Raw question strings to parse dynamically
    private var currentIndex = -1
    private var score = 0
    private lateinit var difficulty: String
    private var startTime: Long = 0
    private var mediaPlayer: MediaPlayer? = null

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
        Log.d("QuickPlay", "Trying to open asset file: $filename")

        try {
            val inputStream = requireContext().assets.open(filename)
            val lines = inputStream.bufferedReader().readLines()

            rawQuestions.clear()

            for ((index, line) in lines.withIndex()) {
                if (line.isBlank()) continue

                val parts = line.split("===")
                if (parts.size < 3) {
                    Log.w("QuickPlay", "Skipping line $index: Invalid format")
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
        if (currentIndex >= rawQuestions.size) {
            endGame()
            return
        }

        // Parse the raw question line: expression === time === bellFlag
        val parts = rawQuestions[currentIndex].split("===")
        if (parts.size < 3) {
            Toast.makeText(requireContext(), "Invalid question format", Toast.LENGTH_SHORT).show()
            loadNextQuestion() // skip invalid
            return
        }

        val rawExpression = parts[0].trim()
        val timeLimit = parts[1].trim().toIntOrNull() ?: 20
        val soundFlag = parts[2].trim().toIntOrNull() ?: 0

        // Parse expression, generate actual question and correct answer
        val (questionText, correctAnswer) = parseExpression(rawExpression)

        // Store this generated question and answer for checking later
        if (questionList.size > currentIndex) {
            questionList[currentIndex] = questionText to correctAnswer
        } else {
            questionList.add(questionText to correctAnswer)
        }

        // Show question and start timer
        binding.questionTv.text = questionText
        speakQuestion(questionText)
        binding.answerEt.text?.clear()
        startTime = System.currentTimeMillis()

        Log.d("QuickPlay", "Question #$currentIndex: $questionText = $correctAnswer (Time limit: $timeLimit s, Sound flag: $soundFlag)")
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) return

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].second

        if (userInput == correctAnswer) {
            score++
            playSound("correct")
            Toast.makeText(requireContext(), "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            playSound("wrong")
            Toast.makeText(requireContext(), "Wrong! Correct answer: $correctAnswer", Toast.LENGTH_SHORT).show()
        }

        loadNextQuestion()
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

    private fun endGame() {
        Toast.makeText(requireContext(), "Quiz Over! Your score: $score", Toast.LENGTH_LONG).show()
        speakQuestion("Quiz over! Your score is $score")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }
}
