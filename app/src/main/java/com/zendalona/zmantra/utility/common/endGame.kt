package com.zendalona.zmantra.utility.common

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.DialogResultBinding

object EndScore {
    fun Fragment.endGameWithScore() {
        val binding = DialogResultBinding.inflate(LayoutInflater.from(requireContext()))

        // Set a simple "Game Finished" message
        binding.messageTextView.text = getString(R.string.game_finished)

        // Pick a random drawable from game over assets
        val gameOverDrawables = listOf(
            R.drawable.dialog_finished_1,
            R.drawable.dialog_finished_2,
            R.drawable.dialog_finished_3
        )
        val drawable = gameOverDrawables.random()

        Glide.with(requireContext())
            .asGif()
            .load(drawable)
            .into(binding.gifImageView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        // Automatically dismiss after 5 seconds and go back
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
            parentFragmentManager.popBackStack()
        }, 5000)
    }
}
