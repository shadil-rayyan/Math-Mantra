package com.zendalona.mathsmantra.utility.common

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.DialogResultBinding

object DialogUtils {

    private val appreciationDict = mapOf(
        "Excellent" to listOf(
            R.string.excellent_1, R.string.excellent_2, R.string.excellent_3, R.string.excellent_4, R.string.excellent_5
        ),
        "Very Good" to listOf(
            R.string.very_good_1, R.string.very_good_2, R.string.very_good_3, R.string.very_good_4, R.string.very_good_5
        ),
        "Good" to listOf(
            R.string.good_1, R.string.good_2, R.string.good_3, R.string.good_4, R.string.good_5
        ),
        "Not Bad" to listOf(
            R.string.not_bad_1, R.string.not_bad_2, R.string.not_bad_3, R.string.not_bad_4, R.string.not_bad_5
        ),
        "Okay" to listOf(
            R.string.okay_1, R.string.okay_2, R.string.okay_3, R.string.okay_4, R.string.okay_5
        ),
        "Wrong" to listOf(
            R.string.wrong_answer // Add more if needed
        )
    )

    private val appreciationDrawables = mapOf(
        "Excellent" to listOf(
            R.drawable.dialog_excellent_1,
            R.drawable.dialog_excellent_2,
            R.drawable.dialog_excellent_3
        ),
        "Very Good" to listOf(
            R.drawable.dialog_very_good_1,
            R.drawable.dialog_very_good_2,
            R.drawable.dialog_very_good_3
        ),
        "Good" to listOf(
            R.drawable.dialog_good_1,
            R.drawable.dialog_good_2,
            R.drawable.dialog_good_3
        ),
        "Not Bad" to listOf(
            R.drawable.dialog_not_bad_1,
            R.drawable.dialog_not_bad_2,
            R.drawable.dialog_not_bad_3
        ),
        "Okay" to listOf(
            R.drawable.dialog_okay_1,
            R.drawable.dialog_okay_2,
            R.drawable.dialog_okay_3
        ),
        "Wrong" to listOf(
            R.drawable.dialog_wrong_anwser_1,
            R.drawable.dialog_wrong_anwser_2,
            R.drawable.dialog_wrong_anwser_3
        )
    )

    fun showResultDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        grade: String,
        onContinue: () -> Unit
    ) {
        val binding = DialogResultBinding.inflate(inflater)

        // Get a random message and drawable for the grade
        val messageRes = appreciationDict[grade]?.random() ?: R.string.wrong_answer
        val drawableRes = appreciationDrawables[grade]?.random()
            ?: appreciationDrawables["Wrong"]!!.random()

        val message = context.getString(messageRes)

        // Speak message
        ttsUtility.speak(message)

        // Show drawable (GIF)
        Glide.with(context)
            .asGif()
            .load(drawableRes)
            .into(binding.gifImageView)

        // Set message text
        binding.messageTextView.text = message

        // Show dialog
        AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_text) { dialog, _ ->
                dialog.dismiss()
                onContinue()
            }
            .create()
            .show()
    }
    fun showRetryDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        message: String,
        onContinue: () -> Unit
    ) {
        val binding = DialogResultBinding.inflate(inflater)

        ttsUtility.speak(message)

        Glide.with(context)
            .asGif()
            .load(R.drawable.wrong)
            .into(binding.gifImageView)

        binding.messageTextView.text = message

        AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_text) { dialog, _ ->
                dialog.dismiss()
                onContinue()
            }
            .create()
            .show()
    }

}
