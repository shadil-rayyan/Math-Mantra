package com.zendalona.mathsmantra.utility.common

import android.R
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.databinding.FragmentEndScoreBinding

class ScorePageFragment : Fragment() {

    private var _binding: FragmentEndScoreBinding? = null
    private val binding get() = _binding!!

    private var score = 0
    private var totalQuestions = 0
    private var percentage = 0.0

    private val handler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable { goBack() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            score = it.getInt("score", 0).coerceAtLeast(0) // No negative score
            totalQuestions = it.getInt("totalQuestions", 0)
            percentage = it.getDouble("percentage", 0.0)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBack()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEndScoreBinding.inflate(inflater, container, false)

        binding.scoreTv.text = "Score: $score"
//        binding.totalQuestionsTv.text = "Total Questions: $totalQuestions"
//        binding.percentageTv.text = "Percentage: ${"%.2f".format(percentage)}%"
//        binding.gradeTv.text = getFinalGrade(percentage)

        // OK button goes back
        binding.okButton.setOnClickListener {
            goBack()
        }

        // Auto-close after 3 seconds
        handler.postDelayed(autoCloseRunnable, 3000)

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            goBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }



    private fun goBack() {
        handler.removeCallbacks(autoCloseRunnable)
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(autoCloseRunnable)
        _binding = null
        super.onDestroyView()
    }
}