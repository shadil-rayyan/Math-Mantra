package com.zendalona.mathsmantra.utility.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R

object EndScore {
    fun Fragment.endGameWithScore(score: Int, totalQuestions: Int) {
        val percentage = if (totalQuestions > 0) {
            (score.toDouble() * 100) / (totalQuestions * 50)
        } else 0.0

        val bundle = Bundle().apply {
            putInt("score", score)
            putInt("totalQuestions", totalQuestions)
            putDouble("percentage", percentage)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ScorePageFragment::class.java, bundle)
            .commit()
    }
}
