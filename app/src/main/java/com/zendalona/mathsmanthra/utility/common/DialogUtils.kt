package com.zendalona.mathsmanthra.utility.common

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.zendalona.mathsmanthra.R
import com.zendalona.mathsmanthra.databinding.DialogResultBinding
import com.zendalona.mathsmanthra.utility.settings.TTSUtility

object DialogUtils {

    fun showResultDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        grade: String,
        onContinue: () -> Unit
    ) {
        val message = when (grade) {
            "Excellent", "Very Good", "Good", "Fair", "Okay" -> "$grade Answer"
            else -> "Wrong Answer"
        }
        val gifRes = if (grade == "Wrong Answer") R.drawable.wrong else R.drawable.right
        ttsUtility.speak(message)

        val binding = DialogResultBinding.inflate(inflater)
        Glide.with(context).asGif().load(gifRes).into(binding.gifImageView)
        binding.messageTextView.text = message

        AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
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
        val gifRes = R.drawable.wrong
        ttsUtility.speak(message)

        val binding = DialogResultBinding.inflate(inflater)
        Glide.with(context).asGif().load(gifRes).into(binding.gifImageView)
        binding.messageTextView.text = message

        AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                onContinue()
            }
            .create()
            .show()
    }
}
