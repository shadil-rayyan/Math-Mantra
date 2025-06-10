package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.databinding.FragmentGameTouchScreenBinding;

import java.util.Random;

public class TouchScreenFragment extends Fragment {

    private FragmentGameTouchScreenBinding binding;
    private Random random;
    private int correctAnswer;
    private boolean answeredCorrectly = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameTouchScreenBinding.inflate(inflater, container, false);


        random = new Random();
        generateNewQuestion();
        setupTouchListener();
        return binding.getRoot();
    }

    private void generateNewQuestion() {
        int num1 = random.nextInt(3) + 1; // 1 to 3
        int num2 = random.nextInt(3) + 1; // 1 to 3
        correctAnswer = num1 + num2;
        answeredCorrectly = false;

        String question = getString(R.string.touchscreen_question_format, num1, num2);
        binding.angleQuestion.setText(question);
    }

    private void setupTouchListener() {
        binding.getRoot().setOnTouchListener((v, event) -> {
            int pointerCount = event.getPointerCount();

            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                String statusText = getString(R.string.touchscreen_fingers_on_screen, pointerCount);
                binding.angleQuestion.setText(statusText);

                if (pointerCount == correctAnswer && !answeredCorrectly) {
                    answeredCorrectly = true;
                    showResultDialog(true);
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!answeredCorrectly) {
                    showResultDialog(false);
                }
            }

            return true;
        });
    }

    private void showResultDialog(boolean isCorrect) {
        int messageRes = isCorrect ? R.string.correct_answer : R.string.wrong_answer;
        int gifResource = isCorrect ? R.drawable.right : R.drawable.wrong;

        LayoutInflater inflater = getLayoutInflater();
        DialogResultBinding dialogBinding = DialogResultBinding.inflate(inflater);
        View dialogView = dialogBinding.getRoot();

        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(dialogBinding.gifImageView);

        dialogBinding.messageTextView.setText(getString(messageRes));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                generateNewQuestion();
            }
        }, 4000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
