package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.databinding.FragmentGameBellBinding;
import com.zendalona.mathsmantra.utility.AccelerometerUtility;
import com.zendalona.mathsmantra.utility.RandomValueGenerator;
import com.zendalona.mathsmantra.utility.SoundEffectUtility;
import com.zendalona.mathsmantra.utility.common.TTSUtility;

public class RingBellFragment extends Fragment {

    private FragmentGameBellBinding binding;
    private AccelerometerUtility accelerometerUtility;
    private SoundEffectUtility soundEffectUtility;
    private RandomValueGenerator randomValueGenerator;
    private TTSUtility tts;
    private boolean gameCompleted = false;
    private int count = 0, target;
    private boolean isShakingAllowed = true;
    private final Handler shakeHandler = new Handler();
    private final Handler gameHandler = new Handler(Looper.getMainLooper());

    public RingBellFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundEffectUtility = SoundEffectUtility.getInstance(requireContext());
        accelerometerUtility = new AccelerometerUtility(requireContext());
        randomValueGenerator = new RandomValueGenerator();
        tts = new TTSUtility(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameBellBinding.inflate(inflater, container, false);
        startGame();
        return binding.getRoot();
    }

    private void ringBell() {
        if (gameCompleted || !isShakingAllowed) return;

        isShakingAllowed = false;
        shakeHandler.postDelayed(() -> isShakingAllowed = true, 500);

        tts.stop(); // Stop current speech if shaking occurs
        binding.bellAnimationView.playAnimation();
        soundEffectUtility.playSound(R.raw.bell_ring);
        count++;
        binding.ringCount.setText(String.valueOf(count));
        tts.speak("Shake count: " + count);

        if (count == target) {
            gameCompleted = true;
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
        showResultDialog("Well done!", R.drawable.right);
    }

    private void showFailureDialog() {
        showResultDialog("Try again!", R.drawable.wrong);
    }

    private void showResultDialog(String message, int gifResource) {
        DialogResultBinding dialogBinding = DialogResultBinding.inflate(getLayoutInflater());
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView);
        dialogBinding.messageTextView.setText(message);

        tts.speak(message + ", Click continue!");

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Continue", (dialog, which) -> {
                    dialog.dismiss();
                    tts.speak("Next question");
                    gameHandler.postDelayed(this::startGame, 1000);
                })
                .create()
                .show();
    }

    private void startGame() {
        gameCompleted = false;
        count = 0;
        binding.ringCount.setText("0");
        binding.bellAnimationView.setVisibility(View.VISIBLE);
        target = randomValueGenerator.generateNumberForCountGame();
        String targetText = "Shake the bell " + target + " times";
        binding.ringMeTv.setText(targetText);

        binding.ringMeTv.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.ringMeTv.setFocusable(true);
        binding.ringMeTv.setFocusableInTouchMode(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.ringMeTv.requestFocus();
            binding.ringMeTv.announceForAccessibility(targetText);
        }, 500);

        tts.speak(targetText);
        gameHandler.postDelayed(() -> isShakingAllowed = true, 3000);
    }

    @Override
    public void onResume() {
        super.onResume();
        accelerometerUtility.registerListener();
        isShakingAllowed = true;

        shakeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isVisible() && !gameCompleted && accelerometerUtility.isDeviceShaken()) {
                    requireActivity().runOnUiThread(RingBellFragment.this::ringBell);
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
