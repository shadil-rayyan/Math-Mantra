package com.zendalona.zmantra.presentation.features.game.sterio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.databinding.FragmentGameSteroBinding
import com.zendalona.zmantra.domain.model.GameQuestion
import java.util.Locale

class SterioFragment : BaseGameFragment() {

    private var binding: FragmentGameSteroBinding? = null
    private var ttsStereo: TextToSpeech? = null

    private var questions: List<GameQuestion> = emptyList()
    private var currentIndex = 0

    private var numA = 0
    private var numB = 0
    private var correctAnswer = 0
    private var questionStartTime = 0L

    override fun getModeName(): String = "sterio"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameSteroBinding.inflate(inflater, container, false)

        binding?.readQuestionBtn?.setOnClickListener { readQuestionAloud() }
        binding?.submitAnswerBtn?.setOnClickListener { submitAnswer() }
        binding?.answerEt?.setOnEditorActionListener { _, _, _ ->
            submitAnswer()
            true
        }
        binding?.readQuestionBtn?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding?.answerEt?.clearFocus()
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding?.answerEt?.windowToken, 0)
            }
        }

        setAccessibilityDescriptions()
        return binding!!.root
    }

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        this.questions = if (questions.isEmpty()) listOf(GameQuestion("5 - 2", 3)) else questions
        currentIndex = 0
        loadNextQuestion()
    }

    private fun loadNextQuestion() {
        if (currentIndex >= questions.size) {
            endGame()
            return
        }

        val question = questions[currentIndex++]
        correctAnswer = question.answer
        questionStartTime = System.currentTimeMillis()

        // Extract numA and numB from expression like "5 - 2"
        val match = Regex("""(\d+)\s*[-+*/]\s*(\d+)""").find(question.expression)
        if (match != null && match.groupValues.size == 3) {
            numA = match.groupValues[1].toInt()
            numB = match.groupValues[2].toInt()
        }

        binding?.answerEt?.setText("")
        announce(binding?.answerEt, getString(R.string.new_question_ready))

        // Focus on read button and read aloud after delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding?.readQuestionBtn?.requestFocus()
            readQuestionAloud()
        }, 500)
    }

    private fun submitAnswer() {
        val input = binding?.answerEt?.text.toString()
        if (input.isEmpty()) {
            announce(binding?.answerEt, getString(R.string.enter_answer_before_submitting))
            Toast.makeText(requireContext(), getString(R.string.enter_answer), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val elapsedTime = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val userAnswer = input.trim()
        val correct = correctAnswer.toString()

        handleAnswerSubmission(
            userAnswer = userAnswer,
            correctAnswer = correct,
            elapsedTime = elapsedTime,
            timeLimit = 15.0,
            onCorrect = { loadNextQuestion() },
            onIncorrect = {},
            onShowCorrect = { loadNextQuestion() }
        )
    }

    private fun readQuestionAloud() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isHeadphoneConnected()) {
            Handler(Looper.getMainLooper()).postDelayed({
                playNumberWithStereo(requireContext(), numA, isRight = false)
            }, 0)

            Handler(Looper.getMainLooper()).postDelayed({
                playTextWithStereo(requireContext(), getString(R.string.minus), isRight = true)
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                playNumberWithStereo(requireContext(), numB, isRight = true)
            }, 6000)
        } else {
            // Fallback: speak using ttsStereo, ensure it's initialized
            if (ttsStereo == null) {
                ttsStereo = TextToSpeech(requireContext()) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        ttsStereo?.language = Locale.ENGLISH
                        val fallbackText = getString(R.string.subtract_numbers, numA, numB)
                        ttsStereo?.speak(fallbackText, TextToSpeech.QUEUE_FLUSH, null, "fallback")
                    }
                }
            } else {
                val fallbackText = getString(R.string.subtract_numbers, numA, numB)
                ttsStereo?.language = Locale.ENGLISH
                ttsStereo?.speak(fallbackText, TextToSpeech.QUEUE_FLUSH, null, "fallback")
            }
        }
    }


    private fun playNumberWithStereo(context: Context, number: Int, isRight: Boolean) {
        if (ttsStereo == null) {
            ttsStereo = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsStereo?.language = Locale.ENGLISH
                    ttsStereo?.speak(
                        getString(R.string.number_is, number),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "stereo"
                    )
                }
            }
        } else {
            ttsStereo?.language = Locale.ENGLISH
            ttsStereo?.speak(
                getString(R.string.number_is, number),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "stereo"
            )
        }
    }

    private fun playTextWithStereo(context: Context, text: String, isRight: Boolean) {
        if (ttsStereo == null) {
            ttsStereo = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsStereo?.language = Locale.ENGLISH
                    ttsStereo?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "stereo_text")
                }
            }
        } else {
            ttsStereo?.language = Locale.ENGLISH
            ttsStereo?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "stereo_text")
        }
    }

    private fun isHeadphoneConnected(): Boolean {
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isWiredHeadsetOn
        }
    }

    private fun setAccessibilityDescriptions() {
        binding?.readQuestionBtn?.contentDescription = getString(R.string.read_question_aloud)
        binding?.answerEt?.contentDescription = getString(R.string.answer_input_field)
        binding?.submitAnswerBtn?.contentDescription = getString(R.string.submit_your_answer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsStereo?.shutdown()
        ttsStereo = null
        binding = null
    }
}
