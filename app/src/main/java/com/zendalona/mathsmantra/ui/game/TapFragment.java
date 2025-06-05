package com.zendalona.mathsmantra.ui.game;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zendalona.mathsmantra.databinding.FragmentGameTapBinding;
import com.zendalona.mathsmantra.utility.common.TTSUtility;

import java.util.Random;

public class TapFragment extends Fragment {

    private FragmentGameTapBinding binding;
    private TTSUtility tts;
    private int count;
    private int targetTaps;
    private Handler handler = new Handler();
    private boolean gameOver = false;

    public TapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TTSUtility(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameTapBinding.inflate(inflater, container, false);

        startNewRound();
        setupGlobalTouchHandler();

        return binding.getRoot();
    }

    private void setupGlobalTouchHandler() {
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !gameOver) {
                onScreenTapped();
            }
            return true;
        });
    }

    private void onScreenTapped() {
        count++;
        binding.tapCount.setText(String.valueOf(count));
        tts.stop();
        tts.speak("Tap count: " + count);

        if (count == targetTaps) {
            gameOver = true;
            tts.speak("Correct! You tapped " + targetTaps + " times.");
            handler.postDelayed(this::startNewRound, 3000);
        } else if (count > targetTaps) {
            gameOver = true;
            tts.speak("Too many taps. The target was " + targetTaps + ". Try again.");
            handler.postDelayed(this::startNewRound, 3000);
        }
    }

    private void startNewRound() {
        count = 0;
        targetTaps = new Random().nextInt(4) + 2; // Random between 2 and 5
        gameOver = false;
        binding.tapCount.setText("0");
        tts.speak("Tap " + targetTaps + " times.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        tts.stop();
        binding = null;
    }
}
