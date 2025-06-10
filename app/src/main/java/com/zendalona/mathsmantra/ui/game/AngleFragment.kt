package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.DialogResultBinding
import com.zendalona.mathsmantra.model.RotationSensorUtility
import java.util.Random

class AngleFragment : Fragment(), RotationSensorUtility.RotationListener {

    private lateinit var rotationTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var rotationSensorUtility: RotationSensorUtility

    private var targetRotation = 0f
    private var baseAzimuth = -1f
    private var questionAnswered = false
    private lateinit var angleUpdateHandler: Handler
    private var angleUpdateRunnable: Runnable? = null

    override fun onCreateView(
            @NonNull inflater: LayoutInflater,
             container: ViewGroup?,
             savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game_angle, container, false)

        rotationTextView = view.findViewById(R.id.rotation_angle_text)
        questionTextView = view.findViewById(R.id.angle_question)

        rotationSensorUtility = RotationSensorUtility(requireContext(), this)
        angleUpdateHandler = Handler(Looper.getMainLooper())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rotationSensorUtility.unregisterListener()
        angleUpdateRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
    }

    override fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float) {
        if (baseAzimuth < 0) {
            baseAzimuth = azimuth
            generateNewQuestion()
            return
        }

        val relativeAzimuth = (azimuth - baseAzimuth + 360) % 360

        if (rotationTextView != null && isAdded) {
            requireActivity().runOnUiThread {
                val angleText = getString(R.string.relative_angle_template, relativeAzimuth.toInt())
                rotationTextView.text = angleText
                checkIfCorrect(relativeAzimuth)
            }
        }
    }

    private fun checkIfCorrect(currentAngle: Float) {
        if (questionAnswered) return

                val isCorrect = Math.abs(targetRotation - currentAngle) <= 10
        if (isCorrect) {
            questionAnswered = true
            angleUpdateRunnable?.let { angleUpdateHandler.removeCallbacks(it) }
            showResultDialog(true)
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val messageResId = if (isCorrect) R.string.right_answer else R.string.wrong_answer
        val gifResId = if (isCorrect) R.drawable.right else R.drawable.wrong

        val dialogBinding = DialogResultBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        Glide.with(this)
                .asGif()
                .load(gifResId)
                .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = getString(messageResId)
        dialogView.announceForAccessibility(getString(messageResId))

        val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
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

    private fun generateNewQuestion() {
        val validAngles = intArrayOf(45, 90, 120, 180, 270)
        targetRotation = validAngles[Random().nextInt(validAngles.size)].toFloat()
        questionAnswered = false

        val question = getString(R.string.turn_to_angle_template, targetRotation.toInt())
        questionTextView.text = question
        questionTextView.announceForAccessibility(question)

        if (angleUpdateRunnable == null) {
            angleUpdateRunnable = Runnable {
                if (!questionAnswered) {
                    val spokenAngle = rotationTextView.text.toString()
                    rotationTextView.announceForAccessibility(
                            getString(R.string.current_angle_announcement, spokenAngle)
                    )
                    angleUpdateHandler.postDelayed(angleUpdateRunnable!!, 2000)
                }
            }
        }
        angleUpdateHandler.postDelayed(angleUpdateRunnable!!, 2000)
    }
}
