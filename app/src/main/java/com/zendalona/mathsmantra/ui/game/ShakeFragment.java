package com.zendalona.mathsmantra.ui.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zendalona.mathsmantra.databinding.FragmentGameShakeBinding;
import com.zendalona.mathsmantra.ui.HintFragment;
import com.zendalona.mathsmantra.utility.AccelerometerUtility;
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences;
import com.zendalona.mathsmantra.utility.settings.LocaleHelper;
import com.zendalona.mathsmantra.utility.common.TTSUtility;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import com.zendalona.mathsmantra.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.Toast;



public class ShakeFragment extends Fragment {

    private FragmentGameShakeBinding binding;
    private AccelerometerUtility accelerometerUtility;
    private TTSUtility tts;
    private int count = 0, target = 0;
    private boolean isShakingAllowed = true;
    private final Handler shakeHandler = new Handler();
    private final Handler gameHandler = new Handler(Looper.getMainLooper());
    private List<String> targetList;
    private int index = 0;
    private String lang;
    private String difficulty;

    public ShakeFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lang = LocaleHelper.getLanguage(requireContext());
        difficulty = DifficultyPreferences.getDifficulty(requireContext());
        tts = new TTSUtility(requireContext());
        accelerometerUtility = new AccelerometerUtility(requireContext());

        targetList = loadShakeQuestionsFromAssets(lang, difficulty);
        if (targetList == null || targetList.isEmpty()) {
            targetList = List.of("2", "3", "4", "5", "6", "7");  // fallback
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameShakeBinding.inflate(inflater, container, false);
        startGame();
        return binding.getRoot();
    }

    private void onShakeDetected() {
        if (!isShakingAllowed) return;

        isShakingAllowed = false;
        shakeHandler.postDelayed(() -> isShakingAllowed = true, 500);

        count++;
        binding.ringCount.setText(String.valueOf(count));

        tts.stop();
        String countText = getString(R.string.shake_count_announcement, count);
        tts.speak(countText);

        if (count == target) {
            evaluateGameResult();
        }
    }

    private void evaluateGameResult() {
        gameHandler.postDelayed(() -> {
            if (count == target) {
                showSuccessDialog();
            } else {
                showFailureDialog();
            }
        }, 2000);
    }

    private void showSuccessDialog() {
        showResultDialog(getString(R.string.shake_success));
    }

    private void showFailureDialog() {
        showResultDialog(getString(R.string.shake_failure));
    }
    private List<String> loadShakeQuestionsFromAssets(String lang, String difficulty) {
        List<String> shakes = new ArrayList<>();
        String fileName =   lang + "/game/shake/" + difficulty.toLowerCase(Locale.ROOT) + ".txt";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    shakes.add(line);
                }
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error loading shake questions: " + fileName, Toast.LENGTH_SHORT).show();
        }

        return shakes;
    }


    private void showResultDialog(String message) {
        binding.ringMeTv.setText(message);

        tts.speak(message + ", " + getString(R.string.shake_continue));

        new android.app.AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton(R.string.shake_continue, (dialog, which) -> {
                    dialog.dismiss();
                    index++;
                    if (index >= targetList.size()) {
                        tts.speak(getString(R.string.shake_game_over));
                        requireActivity().onBackPressed();
                    } else {
                        tts.speak(getString(R.string.shake_next_question));
                        gameHandler.postDelayed(this::startGame, 1000);
                    }
                })
                .create()
                .show();
    }

    private void startGame() {
        count = 0;
        binding.ringCount.setText(getString(R.string.shake_count_initial));
        target = Integer.parseInt(targetList.get(index % targetList.size()));

        String instruction = getString(R.string.shake_target_instruction, target);
        binding.ringMeTv.setText(instruction);
        binding.ringMeTv.setContentDescription(instruction);
        binding.ringMeTv.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.ringMeTv.setFocusable(true);
        binding.ringMeTv.setFocusableInTouchMode(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.ringMeTv.requestFocus();
            binding.ringMeTv.announceForAccessibility(instruction);
        }, 500);

        tts.speak(instruction);
    }

    @Override
    public void onResume() {
        super.onResume();
        accelerometerUtility.registerListener();
        isShakingAllowed = true;

        shakeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isVisible() && accelerometerUtility.isDeviceShaken()) {
                    requireActivity().runOnUiThread(ShakeFragment.this::onShakeDetected);
                }
                shakeHandler.postDelayed(this, 500);
            }
        }, 500);
    }

    public void showHint() {
        Bundle bundle = new Bundle();
        bundle.putString("filepath", "hint/game/shake.txt");  // relative asset path

        HintFragment hintFragment = new HintFragment();
        hintFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, hintFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onPause() {
        super.onPause();
        accelerometerUtility.unregisterListener();
        shakeHandler.removeCallbacksAndMessages(null);
        gameHandler.removeCallbacksAndMessages(null);
        tts.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        tts.shutdown();
    }
}
