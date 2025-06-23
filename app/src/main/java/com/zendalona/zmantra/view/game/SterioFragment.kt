package com.zendalona.zmantra.view.game

import android.app.AlertDialog
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.DialogResultBinding
import com.zendalona.zmantra.databinding.FragmentGameSteroBinding
import com.zendalona.zmantra.model.AudioPlayerUtility
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.utility.RandomValueGenerator
import com.zendalona.zmantra.utility.common.TTSUtility

class SterioFragment : Fragment(), Hintable {

    private var binding: FragmentGameSteroBinding? = null
    private var ttsUtility: TTSUtility? = null
    private var audioPlayerUtility: AudioPlayerUtility? = null
    private var random: RandomValueGenerator? = null

    private var numA = 0
    private var numB = 0
    private var correctAnswer = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameSteroBinding.inflate(inflater, container, false)
        ttsUtility = TTSUtility(requireContext())
        audioPlayerUtility = AudioPlayerUtility()
        random = RandomValueGenerator()
        setHasOptionsMenu(true) // Show menu with hint

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)
        menu.findItem(R.id.action_hint)?.isVisible = true
    }

    private fun generateNewQuestion() {
        // Generate numA and numB between 1 and 9 (matching a1:9*, b1:9*)
        numA = (1..9).random()
        numB = (1..9).random()

        // Subtraction is b - a as per your example {a}-{b} with correctAnswer = b - a
        correctAnswer = numB - numA

        binding?.answerEt?.setText("")
        announce("A new question is ready. Tap 'Read the Question' to listen.")

        // Update UI question text if you have a textview (optional)
    }

    private fun readQuestionAloud() {
        val isHeadphoneConnected = isHeadphoneConnected()
        val isAbove23 = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M

        if (isAbove23 && isHeadphoneConnected) {
            Handler(Looper.getMainLooper()).postDelayed({
                audioPlayerUtility?.playNumberWithStereo(requireContext(), numA, true)
            }, 0)

            Handler(Looper.getMainLooper()).postDelayed({
                ttsUtility?.speak("minus")
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                audioPlayerUtility?.playNumberWithStereo(requireContext(), numB, false)
            }, 6000)
        } else {
            ttsUtility?.speak("Subtract first number $numA from second number $numB")
        }
    }

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
            putString("mode", "sterio") // pass mode
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsUtility?.shutdown()
        binding = null
    }
}
