package com.zendalona.zmantra.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentLearningmodeBinding

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningmodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningmodeBinding.inflate(inflater, container, false)

        setupCategoryButtons()

        return binding.root
    }

    private fun setupCategoryButtons() {
        binding.cardTime.setOnClickListener {
            launchQuickPlay("time", "quickplay")
        }

        binding.cardCurrency.setOnClickListener {
            launchQuickPlay("currency", "quickplay")
        }

        binding.cardDistance.setOnClickListener {
            launchQuickPlay("distance", "quickplay")
        }

        binding.cardAddition.setOnClickListener {
            launchQuickPlay("addition", "quickplay")
        }

        binding.cardSubtraction.setOnClickListener {
            launchQuickPlay("subtraction", "quickplay")
        }

        binding.cardMultiplication.setOnClickListener {
            launchQuickPlay("multiplication", "quickplay")
        }

        binding.cardDivision.setOnClickListener {
            launchQuickPlay("division", "quickplay")
        }

        binding.cardPercentage.setOnClickListener {
            launchQuickPlay("percentage", "quickplay")
        }

        binding.cardRemainder.setOnClickListener {
            launchQuickPlay("remainder", "quickplay")
        }

        binding.cardStory.setOnClickListener {
            launchQuickPlay("story", "quickplay")
        }
    }

    private fun launchQuickPlay(category: String, hintMode: String) {
        val quickPlayFragment = QuickPlayFragment.newInstance(
            category = category,
            hintMode = hintMode
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, quickPlayFragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
