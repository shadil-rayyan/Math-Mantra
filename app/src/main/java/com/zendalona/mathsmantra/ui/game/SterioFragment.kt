package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.DialogResultBinding
import com.zendalona.mathsmantra.databinding.FragmentGameSteroBinding
import com.zendalona.mathsmantra.model.AudioPlayerUtility
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.RandomValueGenerator
import com.zendalona.mathsmantra.utility.common.TTSUtility

class SterioFragment : Fragment(), Hintable {

    private var binding: FragmentGameSteroBinding? = null
    private var ttsUtility: TTSUtility? = null
    private var audioPlayerUtility: AudioPlayerUtility? = null
    private var random: RandomValueGenerator? = null

    private var num1 = 0
    private var num2 = 0
    private var correctAnswer = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameSteroBinding.inflate(inflater, container, false)
        ttsUtility = TTSUtility(requireContext())
        audioPlayerUtility = AudioPlayerUtility()
        random = RandomValueGenerator()

        setAccessibilityDescriptions()
        generateNewQuestion()

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

    private fun generateNewQuestion() {
        val numbers = random!!.generateSubtractionValues()
        num1 = numbers[0]
        num2 = numbers[1]
        correctAnswer = num2- num1

        binding?.answerEt?.setText("")
        announce("A new question is ready. Tap 'Read the Question' to listen.")
    }

    private fun readQuestionAloud() {
        val isHeadphoneConnected = isHeadphoneConnected()
        val isAbove23 = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M

        if (isAbove23 && isHeadphoneConnected) {
            Handler(Looper.getMainLooper()).postDelayed({
                audioPlayerUtility?.playNumberWithStereo(requireContext(), num1, true)
            }, 0)

            Handler(Looper.getMainLooper()).postDelayed({
                ttsUtility?.speak("minus")
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                audioPlayerUtility?.playNumberWithStereo(requireContext(), num2, false)
            }, 6000)
        } else {
            ttsUtility?.speak("substract First number is $num1  from second number  $num2")
        }
    }

    // ✅ Only one definition
    private fun isHeadphoneConnected(): Boolean {
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            devices.any {
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
            generateNewQuestion()
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
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/sterio.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        super.onPause()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        ttsUtility?.shutdown()
        binding = null
    }
}
