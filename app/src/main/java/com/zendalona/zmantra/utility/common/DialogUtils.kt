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
import android.widget.TextView



object DialogUtils {

    private val appreciationDict = mapOf(
        "Excellent" to listOf(
            R.string.excellent_1,
            R.string.excellent_2,
            R.string.excellent_3,
            R.string.excellent_4,
            R.string.excellent_5
        ),
        "Very Good" to listOf(
            R.string.very_good_1,
            R.string.very_good_2,
            R.string.very_good_3,
            R.string.very_good_4,
            R.string.very_good_5
        ),
        "Good" to listOf(
            R.string.good_1,
            R.string.good_2,
            R.string.good_3,
            R.string.good_4,
            R.string.good_5
        ),
        "Not Bad" to listOf(
            R.string.not_bad_1,
            R.string.not_bad_2,
            R.string.not_bad_3,
            R.string.not_bad_4,
            R.string.not_bad_5
        ),
        "Okay" to listOf(
            R.string.okay_1,
            R.string.okay_2,
            R.string.okay_3,
            R.string.okay_4,
            R.string.okay_5
        ),
        "Wrong" to listOf(R.string.wrong_answer)
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

        // Vibration feedback (optional)
        VibrationUtils.vibrate(context, 150)

        val messageRes = appreciationDict[grade]?.random() ?: R.string.wrong_answer
        val drawableRes =
            appreciationDrawables[grade]?.random() ?: appreciationDrawables["Wrong"]!!.random()
        val message = context.getString(messageRes)

        // Load the GIF into the ImageView and enlarge it to occupy 40% of the screen height
        Glide.with(context)
            .asGif()
            .load(drawableRes)
            .into(binding.gifImageView.apply {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT  // Fill screen width
                layoutParams.height =
                    (context.resources.displayMetrics.heightPixels * 0.4).toInt()  // 40% of the screen height
            })

        // Set the message text (which will be positioned above the GIF)
        binding.messageTextView.text = message

        // Create and show the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        // Automatically dismiss the dialog after 4 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onContinue()  // Callback after the dialog is dismissed
            }
        }, 4000)
    }

    fun showRetryDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        message: String,
        onContinue: () -> Unit
    ) {
        val binding = DialogResultBinding.inflate(inflater)
        VibrationUtils.vibrate(context, 300)

        // Load the GIF and enlarge it to 40% of the screen height
        Glide.with(context)
            .asGif()
            .load(R.drawable.wrong)
            .into(binding.gifImageView.apply {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT  // Fill screen width
                layoutParams.height =
                    (context.resources.displayMetrics.heightPixels * 0.4).toInt()  // 40% of the screen height
            })

        // Set the message text (which will be positioned above the GIF)
        binding.messageTextView.text = message

        // Create and show the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        // Automatically dismiss the dialog after 4 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onContinue()
            }
        }, 4000)
    }

    fun showCorrectAnswerDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        correctAnswerText: String,
        onContinue: () -> Unit
    ) {
        val binding = DialogResultBinding.inflate(inflater)
        VibrationUtils.vibrate(context, 400)

        ttsUtility.speak(correctAnswerText)

        // Load the GIF and enlarge it to 40% of the screen height
        Glide.with(context)
            .asGif()
            .load(R.drawable.dialog_wrong_anwser_repeted_1)
            .into(binding.gifImageView.apply {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT  // Fill screen width
                layoutParams.height =
                    (context.resources.displayMetrics.heightPixels * 0.4).toInt()  // 40% of the screen height
            })

        // Set the message text (which will be positioned above the GIF)
        binding.messageTextView.text = correctAnswerText

        // Create and show the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        // Automatically dismiss the dialog after 4 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onContinue()
            }
        }, 4000)
    }

    fun showNextDialog(
        context: Context,
        inflater: LayoutInflater,
        ttsUtility: TTSUtility,
        message: String,
        onContinue: () -> Unit
    ) {
        val binding = DialogResultBinding.inflate(inflater)
        VibrationUtils.vibrate(context, 200)  // Vibration feedback for user interaction

        // Load the GIF and enlarge it to 40% of the screen height
        Glide.with(context)
            .asGif()
            .load(R.drawable.dialog_wrong_anwser_repeted_2)
            .into(binding.gifImageView.apply {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT  // Fill screen width
                layoutParams.height =
                    (context.resources.displayMetrics.heightPixels * 0.4).toInt()  // 40% of the screen height
            })

        // Set the message text (which will be positioned above the GIF)
        binding.messageTextView.text = message

        // Create and show the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        // Automatically dismiss the dialog after 4 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                onContinue()
            }
        }, 4000)
    }
}