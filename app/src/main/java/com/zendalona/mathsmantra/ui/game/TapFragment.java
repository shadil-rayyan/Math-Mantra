package com.zendalona.mathsmantra.ui.game;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.FragmentGameTapBinding;
import com.zendalona.mathsmantra.utility.common.TTSUtility;

public class TapFragment extends Fragment {

    private FragmentGameTapBinding binding;
    private TTSUtility tts;
    private int count;
    private Handler handler = new Handler();

    public TapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TTSUtility(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameTapBinding.inflate(inflater, container, false);

        startGame();

        // Allow tapping anywhere on the screen to increment count
        setupGlobalTouchHandler();

        return binding.getRoot();
    }

    private void setupGlobalTouchHandler() {
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onScreenTapped();
            }
            return true; // Consume all touch events
        });
    }

    private void onScreenTapped() {
        count++;
        binding.tapCount.setText(String.valueOf(count));

        // Stop previous TTS and announce new count
        tts.stop();  // Stop any ongoing TTS
        tts.speak("Tap count: " + count);  // Announce the updated count
    }

    private void startGame() {
        count = 0;
        binding.tapCount.setText("0");

        // Introduce the game with TTS
        tts.speak("Tap anywhere to increase the count. Let's go!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // Stop any delayed tasks
        tts.stop(); // Stop any TTS playback when the view is destroyed
        binding = null;
    }
}
