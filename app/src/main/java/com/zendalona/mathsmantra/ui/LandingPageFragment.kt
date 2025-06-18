package com.zendalona.mathsmantra.ui

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmantra.utility.settings.BackgroundMusicPlayer
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentLandingPageBinding
import com.zendalona.mathsmantra.model.HintIconVisibilityController
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import java.util.Locale

interface FragmentNavigation {
    fun loadFragment(fragment: Fragment, transit: Int)
}

class LandingPageFragment : Fragment(),HintIconVisibilityController {

    override fun shouldShowHintIcon() = false

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    private var navigationListener: FragmentNavigation? = null

    private lateinit var ttsUtility: TTSUtility

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.d("LandingPageFragment", "onCreateView called")

        val lang = LocaleHelper.getLanguage(context) ?: "en"
        val difficulty = DifficultyPreferences.getDifficulty(context)
        Log.d("LandingPageFragment", "Language: $lang, Difficulty: $difficulty")

        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        binding.settings.setOnClickListener {
            Log.d("LandingPageFragment", "Settings button clicked")
            navigationListener?.loadFragment(SettingFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }
        binding.quickplay.setOnClickListener {
            val filePath = "numbers/landingpage/quickplay/${difficulty.lowercase(Locale.ROOT)}.txt"
            Log.d("LandingPageFragment", "Quickplay button clicked, loading file: $filePath")
            QuickPlayFragment.newInstance(filePath).apply {
                navigationListener?.loadFragment(this, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
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
        super.onViewCreated(view, savedInstanceState)
        Log.d("LandingPageFragment", "onViewCreated called")

        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.GONE
        Log.d("LandingPageFragment", "Toolbar hidden")

        BackgroundMusicPlayer.initialize(requireContext())
        Log.d("LandingPageFragment", "BackgroundMusicPlayer initialized")

        ttsUtility = TTSUtility(requireContext())
        val speechRate = prefs.getFloat("tts_speed", 1.0f)
        ttsUtility.setSpeechRate(speechRate)
        Log.d("LandingPageFragment", "TTS initialized with speech rate $speechRate")

        val musicEnabled = prefs.getBoolean("music_enabled", false)
        Log.d("LandingPageFragment", "music_enabled: $musicEnabled")
        if (musicEnabled) {
            BackgroundMusicPlayer.startMusic()
            Log.d("LandingPageFragment", "Background music started")
        } else {
            BackgroundMusicPlayer.pauseMusic()
            Log.d("LandingPageFragment", "Background music paused")
        }

//        ttsUtility.speak("Welcome to the landing page")
        Log.d("LandingPageFragment", "TTS spoke welcome message")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LandingPageFragment", "onPause called - pausing music and stopping TTS")
        BackgroundMusicPlayer.pauseMusic()
        ttsUtility.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LandingPageFragment", "onDestroyView called - stopping music, shutting down TTS, showing toolbar")
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.VISIBLE
        BackgroundMusicPlayer.stopMusic()
        ttsUtility.shutdown()
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
