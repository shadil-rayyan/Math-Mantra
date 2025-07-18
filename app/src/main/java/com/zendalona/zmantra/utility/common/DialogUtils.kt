package com.zendalona.zmantra.utility.common

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.DialogResultBinding

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
        "Wrong" to listOf(R.string.wrong_answer)
    )

    private val appreciationDrawables = mapOf(
        "Excellent" to listOf(
            R.drawable.dialog_excellent_1, R.drawable.dialog_excellent_2, R.drawable.dialog_excellent_3
        ),
        "Very Good" to listOf(
            R.drawable.dialog_very_good_1, R.drawable.dialog_very_good_2, R.drawable.dialog_very_good_3
        ),
        "Good" to listOf(
            R.drawable.dialog_good_1, R.drawable.dialog_good_2, R.drawable.dialog_good_3
        ),
        "Not Bad" to listOf(
            R.drawable.dialog_not_bad_1, R.drawable.dialog_not_bad_2, R.drawable.dialog_not_bad_3
        ),
        "Okay" to listOf(
            R.drawable.dialog_okay_1, R.drawable.dialog_okay_2, R.drawable.dialog_okay_3
        ),
        "Wrong" to listOf(
            R.drawable.dialog_wrong_anwser_1, R.drawable.dialog_wrong_anwser_2, R.drawable.dialog_wrong_anwser_3
        )
    )

    fun showResultDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        grade: String,
        onContinue: () -> Unit
    ) {
        val messageRes = appreciationDict[grade]?.random() ?: R.string.wrong_answer
        val drawableRes = appreciationDrawables[grade]?.random()
            ?: appreciationDrawables["Wrong"]!!.random()

        showCustomDialog(
            context,
            inflater,
            context.getString(messageRes),
            drawableRes,
            vibrationDuration = 150,
            speakText = null,
            onContinue = onContinue
        )
    }

    fun showRetryDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        message: String,
        onContinue: () -> Unit
    ) {
        showCustomDialog(
            context,
            inflater,
            message,
            R.drawable.wrong,
            vibrationDuration = 300,
            speakText = null,
            onContinue = onContinue
        )
    }

    fun showCorrectAnswerDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        correctAnswerText: String,
        onContinue: () -> Unit
    ) {
        showCustomDialog(
            context,
            inflater,
            correctAnswerText,
            R.drawable.dialog_wrong_anwser_repeted_1,
            vibrationDuration = 400,
            speakText = correctAnswerText,
            onContinue = onContinue,
            ttsUtility = ttsUtility
        )
    }

    fun showNextDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        message: String,
        onContinue: () -> Unit
    ) {
        showCustomDialog(
            context,
            inflater,
            message,
            R.drawable.dialog_wrong_anwser_repeted_2,
            vibrationDuration = 200,
            speakText = null,
            onContinue = onContinue
        )
    }

    // --- 🔽 Common Dialog Logic Below 🔽 ---
    private fun showCustomDialog(
        context: Context,
        inflater: LayoutInflater,
        message: String,
        drawableRes: Int,
        vibrationDuration: Long,
        speakText: String? = null,
        onContinue: () -> Unit,
        ttsUtility: TTSUtility? = null
    ) {
        val binding = DialogResultBinding.inflate(inflater)

        VibrationUtils.vibrate(context, vibrationDuration)

        speakText?.let { ttsUtility?.speak(it) }

        Glide.with(context)
            .asGif()
            .load(drawableRes)
            .into(binding.gifImageView.apply {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = (context.resources.displayMetrics.heightPixels * 0.4).toInt()
            })

        binding.messageTextView.text = message

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onContinue()
            }
        }, 4000)
    }
}
