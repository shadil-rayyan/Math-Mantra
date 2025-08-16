package com.zendalona.zmantra.utility

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
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

                // Quit game completely
                activity.finish() // closes all activities
            }
        )
    }
}


