package com.zendalona.mathsmanthra.ui

import android.media.MediaPlayer
import android.os.Bundle
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
import android.speech.tts.TextToSpeech
import kotlin.random.Random

class QuickPlayFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentQuickPlayBinding? = null
    private val binding get() = _binding!!

    private lateinit var tts: TextToSpeech

    private var questionList = mutableListOf<Pair<String, Int>>()  // (question, answer)
    private var currentIndex = -1
    private var score = 0
    private lateinit var difficulty: String
    private var startTime: Long = 0
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(requireContext(), this)
        difficulty = DifficultyPreferences.getDifficulty(requireContext())
        Log.d("QuickPlay", "Loaded difficulty: $difficulty")
        loadQuestionsFromAssets()
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

    private fun loadQuestionsFromAssets() {
        val filename = "quickplay/landingpage/quickplay_${difficulty.lowercase(Locale.ROOT)}.txt"
        Log.d("QuickPlay", "Trying to open asset file: $filename")

        try {
            val inputStream = requireContext().assets.open(filename)
            Log.d("QuickPlay", "Asset file opened successfully")

            val lines = inputStream.bufferedReader().readLines()
            Log.d("QuickPlay", "Read ${lines.size} lines from $filename")

            questionList.clear()  // Clear old questions if any

            for ((index, line) in lines.withIndex()) {
                Log.d("QuickPlay", "Processing line $index: $line")
                val parts = line.split(":")
                if (parts.size < 2) {
                    Log.w("QuickPlay", "Skipping line $index because no ':' found")
                    continue
                }

                val contentParts = parts[1].split("===")
                if (contentParts.size < 2) {
                    Log.w("QuickPlay", "Skipping line $index because no '===' found")
                    continue
                }

                val question = contentParts[0].trim()
                val answer = contentParts[1].trim().toIntOrNull()

                if (answer == null) {
                    Log.w("QuickPlay", "Skipping line $index because answer is not an integer")
                    continue
                }

                questionList.add(question to answer)
                Log.d("QuickPlay", "Added question-answer pair: \"$question\" -> $answer")
            }

            if (questionList.isEmpty()) {
                Log.e("QuickPlay", "No valid questions found after parsing $filename")
                Toast.makeText(requireContext(), "No valid questions found for difficulty $difficulty", Toast.LENGTH_LONG).show()
            } else {
                Log.d("QuickPlay", "Loaded ${questionList.size} valid questions.")
            }

        } catch (e: IOException) {
            Log.e("QuickPlay", "IOException while loading $filename", e)
            Toast.makeText(requireContext(), "Failed to load questions file: $filename", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("QuickPlay", "Unexpected error while loading questions", e)
            Toast.makeText(requireContext(), "Error loading questions: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadNextQuestion() {
        currentIndex++
        if (currentIndex >= questionList.size) {
            endGame()
            return
        }

        val (question, _) = questionList[currentIndex]
        binding.questionTv.text = question
        speakQuestion(question)
        binding.answerEt.text?.clear()
        startTime = System.currentTimeMillis()
    }

    private fun checkAnswer() {
        if (currentIndex >= questionList.size) return

        val userInput = binding.answerEt.text.toString().toIntOrNull()
        val correctAnswer = questionList[currentIndex].second

        if (userInput == correctAnswer) {
            score++
            playSound("correct")
        } else {
            playSound("wrong")
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
