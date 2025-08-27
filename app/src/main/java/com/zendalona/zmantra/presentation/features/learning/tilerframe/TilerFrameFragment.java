package com.zendalona.zmantra.presentation.features.learning.tilerframe;


import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.zmantra.R;
import com.zendalona.zmantra.databinding.DialogResultBinding;
import com.zendalona.zmantra.databinding.FragmentLearningTilerFrameBinding;

import java.util.Random;
public class TilerFrameFragment extends Fragment {

    private FragmentLearningTilerFrameBinding binding;
    private int num1, num2, correctAnswer;
    private char operator;
    private Random random;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLearningTilerFrameBinding.inflate(inflater, container, false);
        random = new Random();

        generateNewQuestion();

        binding.submitBtn.setOnClickListener(v -> checkAnswer());

        // Ensure TalkBack starts from the "What is" title
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding.titleTv != null) {
                binding.titleTv.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
                binding.titleTv.announceForAccessibility(binding.titleTv.getText().toString());
            }
        }, 500); // Delay to ensure layout is ready

        return binding.getRoot();
    }

    private void generateNewQuestion() {
        operator = getRandomOperator();
        num1 = random.nextInt(900) + 100; // Three-digit number
        num2 = random.nextInt(90) + 10;   // Two-digit number

        if (operator == '÷') {
            while (num2 == 0 || num1 % num2 != 0) {
                num2 = random.nextInt(90) + 10; // Ensure clean division
            }
            correctAnswer = num1 / num2;
        } else if (operator == '×') {
            num1 = random.nextInt(90) + 10; // Reduce range to prevent huge values
            correctAnswer = num1 * num2;
        } else if (operator == '-') {
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            correctAnswer = num1 - num2;
        } else {
            correctAnswer = num1 + num2;
        }

        displayNumbers();
    }

    private char getRandomOperator() {
        char[] operators = {'+', '-', '×', '÷'};
        return operators[random.nextInt(operators.length)];
    }

    private void displayNumbers() {
        binding.firstNumberLayout.removeAllViews();
        binding.secondNumberLayout.removeAllViews();
        binding.resultNumberLayout.removeAllViews();

        setNumberBoxes(binding.firstNumberLayout, num1, false);
        setNumberBoxes(binding.secondNumberLayout, num2, false);
        setNumberBoxes(binding.resultNumberLayout, correctAnswer, true);

        binding.operatorTv.setText(String.valueOf(operator));

        // Make sure focus is set to the result field after new question
        binding.resultNumberLayout.getChildAt(0).requestFocus();
    }

    private void setNumberBoxes(LinearLayout layout, int number, boolean isInput) {
        String numStr = String.valueOf(number);

        for (int i = 0; i < numStr.length(); i++) {
            char digit = numStr.charAt(i);
            final int boxIndex = i + 1; // Final variable to capture box number

            if (isInput) {
                EditText inputBox = new EditText(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 120);
                params.setMargins(4, 4, 4, 4);
                inputBox.setLayoutParams(params);
                inputBox.setTextSize(24);
                inputBox.setHint("_");
                inputBox.setGravity(android.view.Gravity.CENTER);
                inputBox.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_activity_bar ));
                inputBox.setBackgroundResource(R.drawable.number_box);

                // Set initial content description
                inputBox.setContentDescription("Answer box " + boxIndex + ", currently empty");

                // Listen for text changes to update content description
                inputBox.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) {
                            inputBox.setContentDescription("Answer box " + boxIndex + ", entered " + s);
                        } else {
                            inputBox.setContentDescription("Answer box " + boxIndex + ", currently empty");
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });

                layout.addView(inputBox);
            } else {
                TextView textBox = new TextView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 120);
                params.setMargins(4, 4, 4, 4);
                textBox.setLayoutParams(params);
                textBox.setTextSize(24);
                textBox.setText(String.valueOf(digit));
                textBox.setGravity(android.view.Gravity.CENTER);
                textBox.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_activity_bar));
                textBox.setBackgroundResource(R.drawable.number_box);
                textBox.setContentDescription("Digit " + digit);
                layout.addView(textBox);
            }
        }
    }


    private void checkAnswer() {
        StringBuilder userAnswerStr = new StringBuilder();
        for (int i = 0; i < binding.resultNumberLayout.getChildCount(); i++) {
            View view = binding.resultNumberLayout.getChildAt(i);
            if (view instanceof EditText) {
                String input = ((EditText) view).getText().toString().trim();
                if (input.isEmpty()) {
                    userAnswerStr.append("0");
                } else {
                    userAnswerStr.append(input);
                }
            }
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(userAnswerStr.toString());
        } catch (NumberFormatException e) {
            userAnswer = -1; // Invalid input
        }

        showResultDialog(userAnswer == correctAnswer);
    }

    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? "Right Answer" : "Wrong Answer";
        int gifResource = isCorrect ? R.drawable.right : R.drawable.wrong;

        LayoutInflater inflater = getLayoutInflater();
        DialogResultBinding dialogBinding = DialogResultBinding.inflate(inflater);
        View dialogView = dialogBinding.getRoot();

        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(dialogBinding.gifImageView);

        dialogBinding.messageTextView.setText(message);

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
