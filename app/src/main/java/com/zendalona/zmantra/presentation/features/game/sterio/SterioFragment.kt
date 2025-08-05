package com.zendalona.zmantra.presentation.features.game.sterio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.base.BaseGameFragment
import com.zendalona.zmantra.databinding.FragmentGameSteroBinding
import com.zendalona.zmantra.domain.model.GameQuestion
import java.io.File
import java.util.Locale

class SterioFragment : BaseGameFragment() {
    private var binding: FragmentGameSteroBinding? = null

    // Renamed the variable to avoid the type conflict with the base class
    private var ttsSynthesizer: TextToSpeech? = null

    private var soundPool: SoundPool? = null
    private val soundMap = HashMap<String, Int>()

    // Initialized in onViewCreated, but not used in the final code
    private var isTtsInitialized = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the new TextToSpeech instance for synthesis
        ttsSynthesizer = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true

                // Get the device's default locale
                val currentLocale = Locale.getDefault()

                // Check if the TTS engine supports the current locale
                val result = ttsSynthesizer?.setLanguage(currentLocale)

                // If the locale is not supported, fall back to English
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsSynthesizer?.language = Locale.ENGLISH
                }

                // This listener tracks when synthesis is done
                ttsSynthesizer?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        val parts = utteranceId?.split("_")
                        if (parts != null && parts.size == 2) {
                            val text = parts[0]
                            val channel = parts[1]
                            loadAndPlayAudio(text, channel)
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        // Log or handle error
                    }
                })
            }
        }

        // Initialize SoundPool for playing audio with low latency
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()
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

        val match = Regex("""(\d+)\s*([-+*/])\s*(\d+)""").find(question.expression)
        if (match != null && match.groupValues.size == 4) {
            numA = match.groupValues[1].toInt()
            numB = match.groupValues[3].toInt()
        }
        binding?.answerEt?.setText("")
        announce(binding?.answerEt, getString(R.string.new_question_ready))

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
        if (isTtsInitialized && isHeadphoneConnected()) {
            val opText = when(val match = Regex("""(\d+)\s*([-+*/])\s*(\d+)""").find(questions[currentIndex-1].expression)) {
                null -> ""
                else -> match.groupValues[2]
            }

            synthesizeText(numA.toString(), "left")

            Handler(Looper.getMainLooper()).postDelayed({
                synthesizeText(opText, "center")
            }, 1500)

            Handler(Looper.getMainLooper()).postDelayed({
                synthesizeText(numB.toString(), "right")
            }, 3000)

        } else {
            val fallbackText = getString(R.string.subtract_numbers, numA, numB)
            ttsSynthesizer?.speak(fallbackText, TextToSpeech.QUEUE_FLUSH, null, "fallback")
        }
    }

    private fun synthesizeText(textToSpeak: String, channel: String) {
        val utteranceId = "${textToSpeak}_$channel"
        val file = File(requireContext().cacheDir, "$utteranceId.wav")
        val params = Bundle()
        ttsSynthesizer?.synthesizeToFile(textToSpeak, params, file, utteranceId)
    }

    private fun loadAndPlayAudio(text: String, channel: String) {
        val file = File(requireContext().cacheDir, "${text}_$channel.wav")
        if (!file.exists()) return

        val soundId = soundPool?.load(file.absolutePath, 1)

        if (soundId != null) {
            soundMap[text] = soundId
        }

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                val leftVolume = when (channel) {
                    "right" -> 0.0f
                    else -> 1.0f
                }
                val rightVolume = when (channel) {
                    "left" -> 0.0f
                    else -> 1.0f
                }

                soundPool?.play(
                    soundId = soundId,
                    leftVolume = leftVolume,
                    rightVolume = rightVolume,
                    priority = 1,
                    loop = 0,
                    rate = 1.0f
                )
            }
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
        // Release resources to prevent memory leaks
        ttsSynthesizer?.shutdown()
        ttsSynthesizer = null
        soundPool?.release()
        soundPool = null
        binding = null
    }
}

private fun SoundPool?.play(
    soundId: Int?,
    leftVolume: Float,
    rightVolume: Float,
    priority: Int,
    loop: Int,
    rate: Float
) {
}
