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
import com.zendalona.mathsmantra.utility.AccelerometerUtility;
import com.zendalona.mathsmantra.utility.RandomValueGenerator;
import com.zendalona.mathsmantra.utility.common.TTSUtility;

public class ShakeFragment extends Fragment {

    private FragmentGameShakeBinding binding;
    private AccelerometerUtility accelerometerUtility;
    private TTSUtility tts;
    private RandomValueGenerator randomValueGenerator;
    private int count = 0, target;
    private boolean isShakingAllowed = true;
    private final Handler shakeHandler = new Handler();
    private final Handler gameHandler = new Handler(Looper.getMainLooper());

    public ShakeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TTSUtility(requireContext());
        accelerometerUtility = new AccelerometerUtility(requireContext());
        randomValueGenerator = new RandomValueGenerator();
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

        // Stop current speech if shaking occurs and speak the count
        tts.stop();  // Stop any ongoing speech
        tts.speak("Shake count: " + count);  // Announce the count after shake

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
        }, 2000); // Delay before showing the result
    }

    private void showSuccessDialog() {
        showResultDialog("Well done!");
    }

    private void showFailureDialog() {
        showResultDialog("Try again!");
    }

    private void showResultDialog(String message) {
        // Show a dialog with the result message
        binding.ringMeTv.setText(message);

        tts.speak(message + ", Click continue!");

        new android.app.AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) -> {
                    dialog.dismiss();
                    tts.speak("Next question");
                    gameHandler.postDelayed(this::startGame, 1000);
                })
                .create()
                .show();
    }

    private void startGame() {
        count = 0;
        binding.ringCount.setText("0");
        target = randomValueGenerator.generateNumberForCountGame();
        String targetText = "Shake the device " + target + " times";
        binding.ringMeTv.setText(targetText);

        binding.ringMeTv.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.ringMeTv.setFocusable(true);
        binding.ringMeTv.setFocusableInTouchMode(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.ringMeTv.requestFocus();
            binding.ringMeTv.announceForAccessibility(targetText);
        }, 500);

        tts.speak(targetText);
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
