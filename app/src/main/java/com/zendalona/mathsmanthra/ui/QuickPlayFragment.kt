package com.zendalona.mathsmanthra.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.zendalona.mathsmanthra.R
import com.zendalona.mathsmanthra.databinding.DialogResultBinding
import com.zendalona.mathsmanthra.databinding.FragmentQuickPlayBinding
import com.zendalona.mathsmanthra.utility.settings.DifficultyPreferences
import kotlin.random.Random

class QuickPlayFragment : Fragment() {
    private var binding: FragmentQuickPlayBinding? = null
    private lateinit var difficulty: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuickPlayBinding.inflate(inflater, container, false)

        difficulty = DifficultyPreferences.getDifficulty(requireContext())
        generateNewQuestion()

        return binding!!.root
    }

    private fun generateNewQuestion() {
        val (num1, num2) = generateAdditionValuesBasedOnDifficulty(difficulty)
        val answer = num1 + num2

        binding!!.answerEt.setText("")
        binding!!.questionTv.text = "$num1 + $num2 = ?"

        val questionDescription = "Math question. $num1 plus $num2 equals what? Double tap to repeat the question."
        binding!!.questionTv.contentDescription = questionDescription
        binding!!.questionTv.post {
            binding!!.questionTv.announceForAccessibility(questionDescription)
        }

        binding!!.submitAnswerBtn.setOnClickListener {
            val input = binding!!.answerEt.text.toString()
            val isCorrect = input.toIntOrNull() == answer
            showResultDialog(isCorrect)
        }
    }

    private fun generateAdditionValuesBasedOnDifficulty(difficulty: String): Pair<Int, Int> {
        val range = when (difficulty.lowercase()) {
            "easy" -> 1..5
            "medium" -> 6..9
            "hard" -> 10..15
            else -> 1..5 // fallback
        }

        val num1 = Random.nextInt(range.first, range.last + 1)
        val num2 = Random.nextInt(range.first, range.last + 1)
        return Pair(num1, num2)
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val message = if (isCorrect) "Right Answer" else "Wrong Answer"
        val gifResource = if (isCorrect) R.drawable.right else R.drawable.wrong

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView)
        dialogBinding.messageTextView.text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                generateNewQuestion()
            }
        }, 4000)
    }
}
