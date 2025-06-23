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
        // Example category buttons - make sure these IDs exist in fragment_landing.xml

        binding.cardTime.setOnClickListener {
            launchQuickPlay("time")  // Excel mode = "math"
        }

        binding.cardCurrency.setOnClickListener {
            launchQuickPlay("currency")
        }

        binding.cardDistance.setOnClickListener {
            launchQuickPlay("distance")
        }

        binding.cardAddition.setOnClickListener {
            launchQuickPlay("addition")
        }
        binding.cardSubtraction.setOnClickListener {
            launchQuickPlay("subtraction")
        }

        binding.cardMultiplication.setOnClickListener {
            launchQuickPlay("multiplication")
        }

        binding.cardDivision.setOnClickListener {
            launchQuickPlay("division")
        }

        binding.cardPercentage.setOnClickListener {
            launchQuickPlay("percentage")
        }
        binding.cardRemainder.setOnClickListener {
            launchQuickPlay("remainder")
        }
        binding.cardStory.setOnClickListener {
            launchQuickPlay("story")
        }

    }

    private fun launchQuickPlay(category: String) {
        val quickPlayFragment = QuickPlayFragment.newInstance(category)

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
