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

import java.util.Random;
import java.util.Stack;

public class MentalCalculationFragment extends Fragment {

    private FragmentGameMentalCalculationBinding binding;
    private String currentExpression;
    private int correctAnswer;
    private boolean isQuestionAnsweredCorrectly = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGameMentalCalculationBinding.inflate(inflater, container, false);
        generateNewQuestion();

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

    private void generateNewQuestion() {
        Random random = new Random();
        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        int num3 = random.nextInt(10) + 1;
        int num4 = random.nextInt(10) + 1;

        String[] operators = {"+", "-", "*", "/"};
        String op1 = operators[random.nextInt(4)];
        String op2 = operators[random.nextInt(4)];
        String op3 = operators[random.nextInt(4)];

        currentExpression = num1 + " " + op1 + " " + num2 + " " + op2 + " " + num3 + " " + op3 + " " + num4;
        correctAnswer = evaluateExpression(currentExpression);

        // Reset view and disable input while showing expression
        isQuestionAnsweredCorrectly = false;
        binding.answerEt.setText("");
        binding.answerEt.setEnabled(false);
        binding.submitAnswerBtn.setEnabled(false);
        binding.mentalCalculation.setText("");

        // Start revealing tokens one at a time
        String[] tokens = currentExpression.split(" ");
        revealTokensSequentially(tokens, 0);
    }

    private void revealTokensSequentially(String[] tokens, int index) {
        if (index >= tokens.length) {
            // Clear the expression from the screen after full reveal
            handler.postDelayed(() -> binding.mentalCalculation.setText(""), 1000);

            // Enable answer input shortly after clearing
            handler.postDelayed(() -> {
                binding.answerEt.setEnabled(true);
                binding.submitAnswerBtn.setEnabled(true);
                binding.answerEt.requestFocus();
            }, 1200); // input enabled after clearing
            return;
        }

        String currentText = binding.mentalCalculation.getText().toString();
        String displayToken = tokens[index].equals("/") ? "÷" : tokens[index];
        binding.mentalCalculation.setText(currentText + " " + displayToken);

        handler.postDelayed(() -> revealTokensSequentially(tokens, index + 1), 1500);
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
            isQuestionAnsweredCorrectly = isCorrect;
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
                    generateNewQuestion(); // New question only if correct
                }
            }
        }, 2000);
    }

    private int evaluateExpression(String expression) {
        return evaluateMath(expression);
    }

    // Math expression evaluator with operator precedence
    private int evaluateMath(String expression) {
        Stack<Integer> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        String[] tokens = expression.split(" ");

        for (String token : tokens) {
            if (isNumber(token)) {
                numbers.push(Integer.parseInt(token));
            } else {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token.charAt(0))) {
                    numbers.push(applyOp(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(token.charAt(0));
            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOp(operators.pop(), numbers.pop(), numbers.pop()));
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
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    private int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                return (b == 0) ? 0 : a / b;
            default:
                return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
