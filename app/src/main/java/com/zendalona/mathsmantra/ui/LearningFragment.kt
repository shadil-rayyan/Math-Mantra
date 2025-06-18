package com.zendalona.mathsmantra.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.zendalona.mathsmantra.databinding.FragmentLearningTilerFrameBinding
import com.zendalona.mathsmantra.databinding.FragmentLearningmodeBinding
import com.zendalona.mathsmantra.model.HintIconVisibilityController
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper

import java.util.Locale

class LearningFragment: Fragment(), HintIconVisibilityController {

    override fun shouldShowHintIcon() = false
    private var binding : FragmentLearningmodeBinding? = null
    private var navigationListener : FragmentNavigation? = null




    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        if (context is FragmentNavigation)
        {
            navigationListener = context as FragmentNavigation

        }
        else
        {
            throw RuntimeException(context.toString()+ "must implement Fragemention Naviagation ")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLearningmodeBinding.inflate(inflater,container, false)
        val lang = LocaleHelper.getLanguage(context)?: "en"
        val difficulty = DifficultyPreferences.getDifficulty(context)

        binding!!.cardTime.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }
        binding!!.cardCurrency.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }
        binding!!.cardDistance.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }

        binding!!.cardAddition.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }
        binding!!.cardSubtraction.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }
        binding!!.cardMultiplication.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }
        binding!!.cardDivision.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }

        }
        binding!!.cardTilerFrame.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
        }

        binding!!.cardPercentage.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
        }


        binding!!.cardRemainder.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LearningPageFragment", "card time clicked ")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
        }



        return binding!!.getRoot()
    }

}