package com.zendalona.zmantra.utility

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.utility.common.DialogUtils
import com.zendalona.zmantra.core.utility.common.TTSUtility
import kotlin.random.Random

object EndScore {

    private var activeDialog: AlertDialog? = null

    /**
     * Show end game result using the shared DialogUtils.showInlineResult
     * Randomly picks a GIF from predefined game-over drawables
     */
    fun endGameWithScore(
        context: Context,
        message: String,
        ttsUtility: TTSUtility? = null
    ) {
        val activity = context as? Activity ?: return
        val rootView = activity.findViewById<View>(android.R.id.content)

        // Random game-over GIF
        val gameOverDrawables = listOf(
            R.drawable.dialog_finished_1,
            R.drawable.dialog_finished_2,
            R.drawable.dialog_finished_3
        )
        val drawableRes = gameOverDrawables.random()

        DialogUtils.showInlineResult(
            context = context,
            rootView = rootView,
            ttsUtility = ttsUtility,
            grade = null,
            message = message,
            drawableRes = drawableRes,
            vibrationDuration = 200,
            speakText = message,
            onComplete = {
                // Dismiss overlay
                val overlay = rootView.findViewById<FrameLayout>(R.id.feedbackOverlay)
                overlay?.visibility = View.GONE

                // Go back to previous screen
                val activity = context as? AppCompatActivity ?: return
                activity.onBackPressedDispatcher.onBackPressed()
            }
        )
    }

    /**
     * Force dismiss end game overlay/dialog
     */
    fun dismissEndGameResult(context: Context) {
        val rootView = (context as? Activity)?.findViewById<View>(android.R.id.content)
        val overlay = rootView?.findViewById<FrameLayout>(R.id.feedbackOverlay)

        if (overlay?.visibility == View.VISIBLE) {
            overlay.visibility = View.GONE
        }

        dismissActiveDialog()
    }

    private fun dismissActiveDialog() {
        activeDialog?.dismiss()
        activeDialog = null
    }
}
