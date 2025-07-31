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
import android.view.View

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
            context, inflater, "Good", ttsUtility, onContinue, message, R.drawable.dialog_good_3, vibrationDuration = 200
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
        val activity = context as? android.app.Activity ?: return
        val rootView = activity.findViewById<View>(android.R.id.content)

        DialogUtils.showInlineResult(
            context = context,
            rootView = rootView,
            ttsUtility = ttsUtility,
            grade = grade,
            message = message,
            drawableRes = drawableRes,
            vibrationDuration = vibrationDuration,
            speakText = speakText,
            onComplete = onContinue
        )
    }
    fun showInlineResult(
        context: Context,
        rootView: View,
        ttsUtility: TTSUtility? = null,
        grade: String? = null,
        message: String? = null,
        drawableRes: Int? = null,
        vibrationDuration: Long = 150,
        speakText: String? = null,
        onComplete: () -> Unit
    ) {
        val overlay = rootView.findViewById<ViewGroup?>(R.id.feedbackOverlay)
        val messageView = rootView.findViewById<android.widget.TextView?>(R.id.messageTextView)
        val gifView = rootView.findViewById<android.widget.ImageView?>(R.id.gifImageView)

        if (overlay != null && messageView != null && gifView != null) {
            android.util.Log.d("DialogUtils", "Showing INLINE result")
            overlay.visibility = android.view.View.VISIBLE

            val dialogData = if (grade != null) appreciationData[grade] else null
            val selectedMessage = message ?: dialogData?.messageRes?.randomOrNull()?.let { context.getString(it) }
            val selectedDrawable = drawableRes ?: dialogData?.drawableRes?.randomOrNull()

            messageView.text = selectedMessage.orEmpty()
            selectedDrawable?.let {
                Glide.with(context).asGif().load(it).into(gifView)
            }

            ttsUtility?.speak(speakText ?: selectedMessage.orEmpty())

            Handler(Looper.getMainLooper()).postDelayed({
                overlay.visibility = android.view.View.GONE
                onComplete()
            }, 2500)
        } else {
            android.util.Log.d("DialogUtils", "Showing DIALOG result (inline views not present)")
            val dialogBinding = DialogResultBinding.inflate(LayoutInflater.from(context))
            dialogBinding.messageTextView.text = message ?: grade
            drawableRes?.let {
                Glide.with(context).asGif().load(it).into(dialogBinding.gifImageView)
            }

            val dialog = AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()

            dialog.show()

            ttsUtility?.speak(speakText ?: message ?: grade ?: "")

            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
                onComplete()
            }, 2500)
        }
    }



}
