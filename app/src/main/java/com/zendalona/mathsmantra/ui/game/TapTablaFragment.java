package com.zendalona.mathsmantra.ui.game;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.MainActivity;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.databinding.FragmentGameDrumBinding;
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityUtils;
import com.zendalona.mathsmantra.utility.RandomValueGenerator;
import com.zendalona.mathsmantra.utility.SoundEffectUtility;
import com.zendalona.mathsmantra.utility.accessibility.MathsManthraAccessibilityService;
import com.zendalona.mathsmantra.utility.common.TTSUtility;
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper;


public class TapTablaFragment extends Fragment {

    private FragmentGameDrumBinding binding;
    private SoundEffectUtility soundEffectUtility;
    private RandomValueGenerator randomValueGenerator;
    private TTSUtility tts;
    private int count, target;
    private boolean talkBackEnabled = false;
    private Handler handler = new Handler();
    private Runnable targetReachedRunnable;

    public TapTablaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundEffectUtility = SoundEffectUtility.getInstance(requireContext());
        talkBackEnabled = AccessibilityUtils.isMathsManthraAccessibilityServiceEnabled(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameDrumBinding.inflate(inflater, container, false);
        randomValueGenerator = new RandomValueGenerator();
        tts = new TTSUtility(requireContext());

        startGame();

        // Allow tapping tabla directly
        binding.tablaAnimationView.setOnClickListener(v -> onTablaTapped());

        // Allow tapping anywhere
        setupGlobalTouchHandler();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (talkBackEnabled) {
            // Call static method directly from AccessibilityHelper
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AccessibilityHelper.disableExploreByTouch((MathsManthraAccessibilityService) requireActivity().getSystemService(AccessibilityService.class));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (talkBackEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AccessibilityHelper.resetExploreByTouch((MathsManthraAccessibilityService) getActivity().getSystemService(AccessibilityService.class));
            }
        }
    }

    private void setupGlobalTouchHandler() {
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                playDrumSound();
            }
            return true; // Consume all touch events
        });
    }

    private void playDrumSound() {
        onTablaTapped();
    }

    private void onTablaTapped() {
        count++;
        binding.tapCount.setText(String.valueOf(count));
        binding.tablaAnimationView.playAnimation();
        soundEffectUtility.playSound(R.raw.drums_sound);

        // Stop previous TTS and announce new count
        tts.stop();  // Use stop() instead of stopSpeaking()
        tts.speak("Tap count: " + count);

        // Check for correct answer or exceeding target
        if (count == target) {
            handler.postDelayed(targetReachedRunnable = () -> appreciateUser(), 3000);
        } else if (count > target) {
            handler.removeCallbacks(targetReachedRunnable);
            showResultDialog(false);
        }
    }



    private void appreciateUser() {
        showResultDialog(true);
    }

    private void showResultDialog(boolean isCorrect) {
        handler.removeCallbacks(targetReachedRunnable);
        String message = isCorrect ? "Well done" : "Try again";
        int gifResource = isCorrect ? R.drawable.right : R.drawable.wrong;

        DialogResultBinding dialogBinding = DialogResultBinding.inflate(getLayoutInflater());
        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView);

        dialogBinding.messageTextView.setText(message);
        tts.speak(message + ". Next question!");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialog.show();

        // Auto-dismiss the dialog after 2 seconds
        handler.postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                startGame(); // Start the next question after the dialog is closed
            }
        }, 2000);
    }


    private void startGame() {
        count = 0;
        target = randomValueGenerator.generateNumberForCountGame();
        String targetText = "Tap the drum " + target + " times";

        // Delay TTS announcement by 4 seconds to allow TalkBack users to process the screen
        handler.postDelayed(() ->
                        tts.speak("This is the drum play screen. " + targetText + ". single tap anywhere to play the drum sound."),
                4000
        );

        binding.tapMeTv.setText(targetText);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // Stop any delayed tasks
        tts.stop(); // Use stop() instead of stopSpeaking()
        binding = null;
    }
}
