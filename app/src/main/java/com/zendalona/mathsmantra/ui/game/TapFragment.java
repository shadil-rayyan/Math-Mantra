package com.zendalona.mathsmantra.ui.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.FragmentGameTapBinding;
import com.zendalona.mathsmantra.utility.common.TTSUtility;
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences;
import com.zendalona.mathsmantra.utility.settings.LocaleHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TapFragment extends Fragment {

    private FragmentGameTapBinding binding;
    private TTSUtility tts;
    private Handler handler = new Handler();
    private int count = 0;
    private int currentIndex = 0;
    private List<Integer> tapQuestions = new ArrayList<>();
    private int targetTaps = 0;
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

        String lang = LocaleHelper.getLanguage(requireContext()); // en, ml etc
        String difficulty = DifficultyPreferences.getDifficulty(requireContext());

        tapQuestions = loadTapQuestionsFromAssets(lang, difficulty);
        if (tapQuestions.isEmpty()) {
            Toast.makeText(getContext(), "No tap questions found!", Toast.LENGTH_LONG).show();
        } else {
            startNewRound();
        }

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
        if (currentIndex >= tapQuestions.size()) {
            tts.speak("Tap activity over.");
            requireActivity().onBackPressed();
            return;
        }

        count = 0;
        gameOver = false;
        targetTaps = tapQuestions.get(currentIndex++);
        binding.tapCount.setText("0");

        // Construct the instruction
        String instruction = "Tap " + targetTaps + " times";

        // Update visual text
        binding.tapMeTv.setText(instruction);

        // Accessibility announcements for TalkBack
        binding.tapMeTv.setContentDescription(instruction);
        binding.tapMeTv.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.tapMeTv.setFocusable(true);
        binding.tapMeTv.setFocusableInTouchMode(true);

        // Announce via TalkBack (delay gives time for screen reader focus)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.tapMeTv.requestFocus();
            binding.tapMeTv.announceForAccessibility(instruction);
        }, 500);

        // Speak using TTS
        tts.speak(instruction);
    }

    private List<Integer> loadTapQuestionsFromAssets(String lang, String difficulty) {
        List<Integer> taps = new ArrayList<>();
        String fileName = lang + "/game/tap/" + difficulty.toLowerCase(Locale.ROOT) + ".txt";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    int value = Integer.parseInt(line.trim());
                    if (value > 0) taps.add(value);
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error loading tap questions: " + fileName, Toast.LENGTH_SHORT).show();
        }

        return taps;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        tts.stop();
        binding = null;
    }
}
