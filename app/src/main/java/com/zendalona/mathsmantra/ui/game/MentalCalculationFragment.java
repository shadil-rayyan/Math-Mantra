package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathmantra.R;
import com.zendalona.mathmantra.databinding.DialogResultBinding;
import com.zendalona.mathmantra.databinding.FragmentMentalCalculationBinding;

import java.util.Locale;
import java.util.Random;
import java.util.Stack;

public class MentalCalculationFragment extends Fragment {

    private FragmentMentalCalculationBinding binding;
    private String currentExpression;
    private int correctAnswer;
    private TextToSpeech textToSpeech;
    private boolean isQuestionAnsweredCorrectly = false; // Track correctness

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMentalCalculationBinding.inflate(inflater, container, false);
        setupTextToSpeech();
        generateNewQuestion();

        binding.submitAnswerBtn.setOnClickListener(v -> checkAnswer());

        // Auto-submit on keyboard 'tick' or 'right' button press
        binding.answerEt.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer();
                return true;
            }
            return false;
        });

        return binding.getRoot();
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
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

        String displayText = formatExpressionForDisplay(currentExpression) + " = ?";
        binding.mentalCalculation.setText(displayText);

        // Accessibility description
        String spokenExpression = "Math question: " + formatExpressionForSpeech(currentExpression) + ". What is the answer?";
        binding.mentalCalculation.setContentDescription(spokenExpression);

        // Read the question aloud when the screen opens
        speak(spokenExpression);

        isQuestionAnsweredCorrectly = false; // Reset correctness tracker
    }

    private int evaluateExpression(String expression) {
        return evaluateMath(expression);
    }

    private void checkAnswer() {
        String userInput = binding.answerEt.getText().toString();
        if (userInput.isEmpty()) {
            Toast.makeText(getContext(), "Enter your answer", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCorrect = Integer.parseInt(userInput) == correctAnswer;
        isQuestionAnsweredCorrectly = isCorrect;
        showResultDialog(isCorrect);
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
        speak(message); // Read the result message aloud

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                if (isCorrect) {
                    generateNewQuestion(); // Only change question if correct
                }
            }
        }, 2000);
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private String formatExpressionForDisplay(String expression) {
        return expression.replace("/", "÷");
    }

    private String formatExpressionForSpeech(String expression) {
        return expression.replace("+", " plus ")
                .replace("-", " minus ")
                .replace("*", " times ")
                .replace("/", " divided by ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        binding = null;
    }

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
                return (b == 0) ? 0 : a / b; // Handle division by zero
            default:
                return 0;
        }
    }

}
