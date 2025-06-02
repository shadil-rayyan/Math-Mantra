package com.zendalona.mathsmathra.ui

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmathra.R
import com.zendalona.mathsmathra.databinding.FragmentLandingPageBinding
import com.zendalona.mathsmathra.utility.settings.BackgroundMusicPlayer
import java.util.Locale

interface FragmentNavigation {
    fun loadFragment(fragment: Fragment, transit: Int)
}

class LandingPageFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    private var navigationListener: FragmentNavigation? = null

    private lateinit var backgroundMusicPlayer: BackgroundMusicPlayer
    private var tts: TextToSpeech? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        binding.settings.setOnClickListener {
            navigationListener?.loadFragment(SettingFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.GONE

        // Initialize BackgroundMusicPlayer
        backgroundMusicPlayer = BackgroundMusicPlayer(requireContext())

        // Initialize TextToSpeech
        tts = TextToSpeech(requireContext(), this)

        // Start music if enabled in preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (prefs.getBoolean("music_enabled", false)) {
            backgroundMusicPlayer.startMusic()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()

            // Speak a welcome message as a test
            tts?.speak("Welcome to the landing page", TextToSpeech.QUEUE_FLUSH, null, "welcome_msg")
        } else {
            // Handle initialization failure if needed
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause music and stop TTS when fragment is paused
        backgroundMusicPlayer.pauseMusic()
        tts?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.VISIBLE

        // Stop music and shutdown TTS when fragment destroyed
        backgroundMusicPlayer.stopMusic()

        tts?.shutdown()
        tts = null

        _binding = null
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
