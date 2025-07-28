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

    private val appreciationData = mapOf(
        "Excellent" to DialogData(
            messageRes = listOf(R.string.excellent_1, R.string.excellent_2, R.string.excellent_3, R.string.excellent_4, R.string.excellent_5),
            drawableRes = listOf(R.drawable.dialog_excellent_1, R.drawable.dialog_excellent_2, R.drawable.dialog_excellent_3)
        ),
        "Very Good" to DialogData(
            messageRes = listOf(R.string.very_good_1, R.string.very_good_2, R.string.very_good_3, R.string.very_good_4, R.string.very_good_5),
            drawableRes = listOf(R.drawable.dialog_very_good_1, R.drawable.dialog_very_good_2, R.drawable.dialog_very_good_3)
        ),
        "Good" to DialogData(
            messageRes = listOf(R.string.good_1, R.string.good_2, R.string.good_3, R.string.good_4, R.string.good_5),
            drawableRes = listOf(R.drawable.dialog_good_1, R.drawable.dialog_good_2, R.drawable.dialog_good_3)
        ),
        "Not Bad" to DialogData(
            messageRes = listOf(R.string.not_bad_1, R.string.not_bad_2, R.string.not_bad_3, R.string.not_bad_4, R.string.not_bad_5),
            drawableRes = listOf(R.drawable.dialog_not_bad_1, R.drawable.dialog_not_bad_2, R.drawable.dialog_not_bad_3)
        ),
        "Okay" to DialogData(
            messageRes = listOf(R.string.okay_1, R.string.okay_2, R.string.okay_3, R.string.okay_4, R.string.okay_5),
            drawableRes = listOf(R.drawable.dialog_okay_1, R.drawable.dialog_okay_2, R.drawable.dialog_okay_3)
        ),
        "Wrong" to DialogData(
            messageRes = listOf(R.string.wrong_answer),
            drawableRes = listOf(R.drawable.dialog_wrong_anwser_1, R.drawable.dialog_wrong_anwser_2, R.drawable.dialog_wrong_anwser_3)
        )
    )

    data class DialogData(val messageRes: List<Int>, val drawableRes: List<Int>)

    fun showResultDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        grade: String,
        onContinue: () -> Unit
    ) {
        showCustomDialog(
            context, inflater, grade, ttsUtility, onContinue, vibrationDuration = 150
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
            context, inflater, "Wrong", ttsUtility, onContinue, message, R.drawable.wrong, vibrationDuration = 300
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
            context, inflater, "Wrong", ttsUtility, onContinue, correctAnswerText, R.drawable.dialog_wrong_anwser_repeted_1, vibrationDuration = 400, speakText = correctAnswerText
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
            context, inflater, "Wrong", ttsUtility, onContinue, message, R.drawable.dialog_wrong_anwser_repeted_2, vibrationDuration = 200
        )
    }

    // --- 🔽 Common Dialog Logic Below 🔽 ---
    private fun showCustomDialog(
        context: Context,
        inflater: LayoutInflater,
        grade: String? = null,
        ttsUtility: TTSUtility? = null,
        onContinue: () -> Unit,
        message: String? = null,
        drawableRes: Int? = null,
        vibrationDuration: Long = 150,
        speakText: String? = null
    ) {
        val binding = DialogResultBinding.inflate(inflater)
        val gradeData = grade?.let { appreciationData[it] } ?: appreciationData["Wrong"]!!

        // Set the message and drawable based on grade or provided values
        val finalMessage = message ?: context.getString(gradeData.messageRes.random())
        val finalDrawableRes = drawableRes ?: gradeData.drawableRes.random()

        VibrationUtils.vibrate(context, vibrationDuration)
        speakText?.let { ttsUtility?.speak(it) }

        Glide.with(context)
            .asGif()
            .load(finalDrawableRes)
            .into(binding.gifImageView.apply {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = (context.resources.displayMetrics.heightPixels * 0.4).toInt()
            })

        binding.messageTextView.text = finalMessage

//        val previousFocus = (context as? android.app.Activity)?.currentFocus

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
//                previousFocus?.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED)
                onContinue()
            }
        }, 4000)
    }
}
