package com.zendalona.zmantra.presentation.features.landing

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.utility.common.TTSUtility
import com.zendalona.zmantra.core.utility.settings.BackgroundMusicPlayer
import com.zendalona.zmantra.core.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.core.utility.settings.LocaleHelper
import com.zendalona.zmantra.databinding.FragmentLandingPageBinding
import com.zendalona.zmantra.presentation.features.game.GameFragment
import com.zendalona.zmantra.presentation.features.learning.LearningFragment
import com.zendalona.zmantra.presentation.features.quickplay.QuickPlayFragment
import com.zendalona.zmantra.presentation.features.setting.SettingFragment
import com.zendalona.zmantra.presentation.features.userguide.UserGuideFragment

interface FragmentNavigation {
    fun loadFragment(fragment: Fragment, transit: Int)
}

class LandingPageFragment : Fragment() {

    private var _binding: FragmentLandingPageBinding? = null
    val binding get() = _binding!!

    var navigationListener: FragmentNavigation? = null

    // Use nullable type for ttsUtility
    private var ttsUtility: TTSUtility? = null

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.d("LandingPageFragment", "onCreateView called")

        val lang = LocaleHelper.getLanguage(context)
        val difficulty = DifficultyPreferences.getDifficulty(context)
        Log.d("LandingPageFragment", "Language: $lang, Difficulty: $difficulty")

        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        binding.settings.setOnClickListener {
            Log.d("LandingPageFragment", "Settings button clicked")
            navigationListener?.loadFragment(SettingFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        binding.quickplay.setOnClickListener {
            Log.d("LandingPageFragment", "Quickplay button clicked")
            val fragment = QuickPlayFragment.newInstance("quickplay","quickplay")
            navigationListener?.loadFragment(fragment, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        binding.learningButton.setOnClickListener {
            Log.d("LandingPageFragment", "Learning button clicked")
            navigationListener?.loadFragment(LearningFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        binding.GameButton.setOnClickListener {
            Log.d("LandingPageFragment", "Game button clicked")
            navigationListener?.loadFragment(GameFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        binding.userGuide.setOnClickListener {
            Log.d("LandingPageFragment", "User Guide button clicked")
            navigationListener?.loadFragment(UserGuideFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        binding.quitbutton.setOnClickListener {
            Log.d("LandingPageFragment", "Quit button clicked. Finishing activity.")
            activity?.finish()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("LandingPageFragment", "onViewCreated called")

        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.GONE
        Log.d("LandingPageFragment", "Toolbar hidden")

        BackgroundMusicPlayer.initialize(requireContext())
        Log.d("LandingPageFragment", "BackgroundMusicPlayer initialized")

        // Initialize TTS only if it is not already initialized
        if (ttsUtility == null) {
            ttsUtility = TTSUtility(requireContext())
            val speechRate = prefs.getFloat("tts_speed", 1.0f)
            ttsUtility?.setSpeechRate(speechRate)
            Log.d("LandingPageFragment", "TTS initialized with speech rate $speechRate")
        }

        val musicEnabled = prefs.getBoolean("music_enabled", false)
        Log.d("LandingPageFragment", "music_enabled: $musicEnabled")
        if (musicEnabled) {
            BackgroundMusicPlayer.startMusic()
            Log.d("LandingPageFragment", "Background music started")
        } else {
            BackgroundMusicPlayer.pauseMusic()
            Log.d("LandingPageFragment", "Background music paused")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("LandingPageFragment", "onPause called - pausing music and stopping TTS")
        BackgroundMusicPlayer.pauseMusic()

        // Safely access ttsUtility by checking if it's initialized
        ttsUtility?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LandingPageFragment", "onDestroyView called - stopping music, shutting down TTS, showing toolbar")
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.VISIBLE
        BackgroundMusicPlayer.stopMusic()

        // Safely shutdown TTS if initialized
        ttsUtility?.shutdown()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("LandingPageFragment", "onAttach called")
        if (context is FragmentNavigation) {
            navigationListener = context
            Log.d("LandingPageFragment", "navigationListener attached")
        } else {
            Log.d("LandingPageFragment", "Context does not implement FragmentNavigation")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("LandingPageFragment", "onDetach called - clearing navigationListener")
        navigationListener = null
    }
}
