package com.zendalona.mathsmanthra.ui

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
import com.zendalona.mathsmanthra.utility.settings.BackgroundMusicPlayer
import com.zendalona.mathsmanthra.utility.settings.TTSUtility
import com.zendalona.mathsmanthra.R
import com.zendalona.mathsmanthra.databinding.FragmentLandingPageBinding

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
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        // Navigate to SettingFragment when clicking settings button
        binding.settings.setOnClickListener {
            navigationListener?.loadFragment(SettingFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }
        binding.quickplay.setOnClickListener {
            val fragment = QuickPlayFragment.newInstance("landingpage")
            navigationListener?.loadFragment(fragment, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide toolbar on landing page
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.GONE

        BackgroundMusicPlayer.initialize(requireContext())

        ttsUtility = TTSUtility(requireContext())

        val speechRate = prefs.getFloat("tts_speed", 1.0f)
        ttsUtility.setSpeechRate(speechRate)

        Log.d("LandingPageFragment", "music_enabled: ${prefs.getBoolean("music_enabled", false)}")

        if (prefs.getBoolean("music_enabled", false)) {
            BackgroundMusicPlayer.startMusic()
        } else {
            BackgroundMusicPlayer.pauseMusic()
        }

        ttsUtility.speak("Welcome to the landing page")
    }

    override fun onPause() {
        super.onPause()
        BackgroundMusicPlayer.pauseMusic()
        ttsUtility.stop()
        Log.d("LandingPageFragment", "onPause: music paused, TTS stopped")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show toolbar again when leaving landing page
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.VISIBLE

        BackgroundMusicPlayer.stopMusic()
        ttsUtility.shutdown()
        _binding = null

        Log.d("LandingPageFragment", "onDestroyView: music stopped, TTS shutdown")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentNavigation) {
            navigationListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }
}
