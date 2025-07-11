package com.zendalona.zmantra.view

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
import com.zendalona.zmantra.utility.settings.BackgroundMusicPlayer
import com.zendalona.zmantra.utility.common.TTSUtility
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentLandingPageBinding
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import com.zendalona.zmantra.utility.settings.LocaleHelper

interface FragmentNavigation {
    fun loadFragment(fragment: Fragment, transit: Int)
}

class LandingPageFragment : Fragment() {

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
            Log.d("LandingPageFragment", "Quickplay button clicked")
            val fragment = QuickPlayFragment.newInstance("quickplay")
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

        binding!!.quickplay.post {
            binding!!.quickplay.requestFocus() // This ensures TalkBack will focus on the button
        }


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
