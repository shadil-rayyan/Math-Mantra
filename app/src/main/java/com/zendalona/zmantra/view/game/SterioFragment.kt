package com.zendalona.zmantra.view.game

import android.app.AlertDialog
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.DialogResultBinding
import com.zendalona.zmantra.databinding.FragmentGameSteroBinding
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences.getDifficulty
import com.zendalona.zmantra.utility.settings.LocaleHelper.getLanguage
import com.zendalona.zmantra.view.HintFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SterioFragment : Fragment(), Hintable {

    private var binding: FragmentGameSteroBinding? = null
    private var ttsUtility: TTSUtility? = null
    private var ttsStereo: TextToSpeech? = null

    private var questions: List<GameQuestion> = emptyList()
    private var currentIndex = 0

    private var numA = 0
    private var numB = 0
    private var correctAnswer = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameSteroBinding.inflate(inflater, container, false)
        ttsUtility = TTSUtility(requireContext())
        setHasOptionsMenu(true)

        val lang = getLanguage(requireContext()).ifEmpty { "en" }
        val difficulty = getDifficulty(requireContext()).toString()

        lifecycleScope.launch {
            questions = withContext(Dispatchers.IO) {
                ExcelQuestionLoader.loadQuestionsFromExcel(
                    context = requireContext(),
                    lang = lang,
                    mode = "sterio",
                    difficulty = difficulty
                ).shuffled()
            }

            if (isAdded && isResumed) {
                if (questions.isEmpty()) {
                    Toast.makeText(requireContext(), "No stereo questions found.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                setAccessibilityDescriptions()
                loadNextQuestion()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding?.readQuestionBtn?.requestFocus()
            readQuestionAloud()
        }, 500)

        binding?.readQuestionBtn?.setOnClickListener { readQuestionAloud() }
        binding?.submitAnswerBtn?.setOnClickListener { submitAnswer() }
        binding?.answerEt?.setOnEditorActionListener { _, _, _ ->
            submitAnswer()
            true
        }

        return binding!!.root
    }

    private fun loadNextQuestion() {
        if (currentIndex >= questions.size) {
            Toast.makeText(requireContext(), "All questions completed!", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        val question = questions[currentIndex++]
        correctAnswer = question.answer
        binding?.answerEt?.setText("")

        val match = Regex("""(\d+)\s*[-+*/]\s*(\d+)""").find(question.expression)
        if (match != null && match.groupValues.size == 3) {
            numA = match.groupValues[1].toInt()
            numB = match.groupValues[2].toInt()
        } else {
            numA = 0
            numB = 0
        }

        announce("A new question is ready. Tap 'Read the Question' to listen.")
    }

    private fun readQuestionAloud() {
        val isHeadphoneConnected = isHeadphoneConnected()
        val isAbove23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        if (isAbove23 && isHeadphoneConnected) {
            Handler(Looper.getMainLooper()).postDelayed({
                playNumberWithStereo(requireContext(), numA, isRight = false)
            }, 0)

            Handler(Looper.getMainLooper()).postDelayed({
                ttsUtility?.speak("minus")
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                playNumberWithStereo(requireContext(), numB, isRight = true)
            }, 6000)
        } else {
            ttsUtility?.speak("Subtract second number $numB from first number $numA")
        }
    }

    private fun playNumberWithStereo(context: Context, number: Int, isRight: Boolean) {
        if (ttsStereo == null) {
            ttsStereo = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsStereo?.language = Locale.ENGLISH
                    // NOTE: No public stereo panning API in TTS. Consider using SoundPool or AudioTrack for actual stereo effects.
                    ttsStereo?.speak("The number is $number", TextToSpeech.QUEUE_FLUSH, null, "stereo")
                }
            }
        } else {
            ttsStereo?.speak("The number is $number", TextToSpeech.QUEUE_FLUSH, null, "stereo")
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

    private fun submitAnswer() {
        val userInput = binding?.answerEt?.text.toString()
        if (userInput.isEmpty()) {
            announce("Please enter an answer before submitting.")
            Toast.makeText(requireContext(), "Please enter an answer!", Toast.LENGTH_SHORT).show()
            return
        }

        val isCorrect = userInput.toIntOrNull() == correctAnswer
        showResultDialog(isCorrect)
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val message = if (isCorrect) "Right Answer!" else "Wrong Answer. Try again."
        val gifResource = if (isCorrect) R.drawable.right else R.drawable.wrong

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView)
        dialogBinding.messageTextView.text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.show()
        dialogBinding.root.announceForAccessibility(message)

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            loadNextQuestion()
        }, 2000)
    }

    private fun setAccessibilityDescriptions() {
        binding?.readQuestionBtn?.contentDescription = "Read question aloud."
        binding?.answerEt?.contentDescription = "Answer input field."
        binding?.submitAnswerBtn?.contentDescription = "Submit your answer."
    }

    private fun announce(message: String) {
        binding?.answerEt?.announceForAccessibility(message)
    }

    override fun showHint() {
        val bundle = Bundle().apply { putString("mode", "sterio") }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsUtility?.shutdown()
        ttsStereo?.shutdown()
        ttsStereo = null
        binding = null
    }
}
