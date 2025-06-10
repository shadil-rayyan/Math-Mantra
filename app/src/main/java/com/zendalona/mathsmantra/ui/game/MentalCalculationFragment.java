package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.databinding.FragmentGameMentalCalculationBinding;
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences;
import com.zendalona.mathsmantra.utility.settings.LocaleHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class MentalCalculationFragment extends Fragment {

    private FragmentGameMentalCalculationBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<String> expressionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String currentExpression;
    private int correctAnswer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGameMentalCalculationBinding.inflate(inflater, container, false);

        String lang = LocaleHelper.getLanguage(requireContext()); // "en", "ml", etc.
        String difficulty = DifficultyPreferences.getDifficulty(requireContext());

        expressionList = loadExpressionsFromAssets(lang, difficulty);

        if (!expressionList.isEmpty()) {
            loadNextQuestion();
        } else {
            Toast.makeText(getContext(), "No questions available!", Toast.LENGTH_SHORT).show();
        }

        binding.submitAnswerBtn.setOnClickListener(v -> checkAnswer());

        binding.answerEt.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer();
                return true;
            }
            return false;
        });

        return binding.getRoot();
    }

    private List<String> loadExpressionsFromAssets(String lang, String difficulty) {
        List<String> expressions = new ArrayList<>();
        String fileName = lang + "/game/mentalcalculation/" + difficulty.toLowerCase(Locale.ROOT) + ".txt";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open(fileName)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    expressions.add(line.trim());
                }
            }

            if (expressions.isEmpty()) {
                Toast.makeText(getContext(), "No expressions found in " + fileName, Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to load expressions from: " + fileName, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return expressions;
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= expressionList.size()) {
            Toast.makeText(getContext(), "Activity over", Toast.LENGTH_SHORT).show();
            if (requireActivity() != null) requireActivity().onBackPressed();
            return;
        }

        currentExpression = expressionList.get(currentQuestionIndex);
        correctAnswer = evaluateExpression(currentExpression);
        currentQuestionIndex++;

        binding.answerEt.setText("");
        binding.answerEt.setEnabled(false);
        binding.submitAnswerBtn.setEnabled(false);
        binding.mentalCalculation.setText("");

        String[] tokens = currentExpression.split(" ");
        revealTokensOneByOne(tokens, 0);
    }

    private void revealTokensOneByOne(String[] tokens, int index) {
        if (index >= tokens.length) {
            handler.postDelayed(() -> {
                binding.answerEt.setEnabled(true);
                binding.submitAnswerBtn.setEnabled(true);
                binding.answerEt.requestFocus();
            }, 500);
            return;
        }

        String token = tokens[index].equals("/") ? "÷" : tokens[index];

        // Set token for accessibility
        binding.mentalCalculation.setText(token);
        binding.mentalCalculation.setContentDescription(token);
        binding.mentalCalculation.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.mentalCalculation.setFocusable(true);
        binding.mentalCalculation.setFocusableInTouchMode(true);

        handler.postDelayed(() -> {
            binding.mentalCalculation.requestFocus();
            binding.mentalCalculation.announceForAccessibility(token);

            // After short delay, clear and move to next token
            handler.postDelayed(() -> {
                binding.mentalCalculation.setText("");
                revealTokensOneByOne(tokens, index + 1);
            }, 1200); // Delay after speaking one token
        }, 200); // Delay before focusing and speaking
    }

    private void checkAnswer() {
        String userInput = binding.answerEt.getText().toString().trim();
        if (userInput.isEmpty()) {
            Toast.makeText(getContext(), "Enter your answer", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int userAnswer = Integer.parseInt(userInput);
            boolean isCorrect = (userAnswer == correctAnswer);
            showResultDialog(isCorrect);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? "Correct Answer!" : "Wrong Answer! Try again.";
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

        handler.postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                if (isCorrect) {
                    loadNextQuestion();
                }
            }
        }, 2000);
    }

    private int evaluateExpression(String expression) {
        Stack<Integer> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        String[] tokens = expression.split(" ");

        for (String token : tokens) {
            if (isNumber(token)) {
                numbers.push(Integer.parseInt(token));
            } else {
                char op = token.charAt(0);
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(op)) {
                    int b = numbers.pop();
                    int a = numbers.pop();
                    numbers.push(applyOp(operators.pop(), a, b));
                }
                operators.push(op);
            }
        }

        while (!operators.isEmpty()) {
            int b = numbers.pop();
            int a = numbers.pop();
            numbers.push(applyOp(operators.pop(), a, b));
        }

        return numbers.pop();
    }

    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int precedence(char op) {
        switch (op) {
            case '+':
            case '-': return 1;
            case '*':
            case '/': return 2;
            default: return -1;
        }
    }

    private int applyOp(char op, int a, int b) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return b == 0 ? 0 : a / b;
            default: return 0;
        }
    }
}
