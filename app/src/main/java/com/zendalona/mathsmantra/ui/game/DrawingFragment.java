package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.databinding.FragmentGameDrawingBinding;
import com.zendalona.mathsmantra.view.DrawingView;

import java.util.Random;

public class DrawingFragment extends Fragment {

    private FragmentGameDrawingBinding binding;
    private String currentShape; // "triangle" or "rectangle"
    private DrawingView drawingView;
    private AccessibilityManager accessibilityManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameDrawingBinding.inflate(inflater, container, false);

        // Initialize Accessibility Manager
        accessibilityManager = (AccessibilityManager) requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE);

        // Create a custom DrawingView for drawing
        drawingView = new DrawingView(requireContext());
        binding.drawingContainer.addView(drawingView); // Add it to the layout

        // Generate first question
        generateNewQuestion();

        // Reset Button
        binding.resetButton.setOnClickListener(v -> {
            drawingView.clearCanvas();
            announce("Canvas cleared. You can draw again.");
        });

        // Submit Button - Checks the drawing only when clicked
        binding.submitButton.setOnClickListener(v -> checkDrawing());

        return binding.getRoot();
    }

    private void generateNewQuestion() {
        // Randomly choose "triangle" or "rectangle"
        currentShape = new Random().nextBoolean() ? "triangle" : "rectangle";
        String instruction = "Draw a " + currentShape;

        // Update TextView and Content Description
        binding.questionText.setText(instruction);
        String questionDescription = "Drawing task. " + instruction + ". Use touch gestures to draw.";
        binding.questionText.setContentDescription(questionDescription);

        // Announce the updated instruction for TalkBack
        binding.questionText.post(() -> binding.questionText.announceForAccessibility(questionDescription));

        // Clear the drawing canvas
        drawingView.clearCanvas();
    }

    private void checkDrawing() {
        // Get points drawn from drawingView
        // We will use points and expected corners based on currentShape
        int expectedCorners;
        if ("triangle".equalsIgnoreCase(currentShape)) {
            expectedCorners = 3;
        } else if ("rectangle".equalsIgnoreCase(currentShape)) {
            expectedCorners = 4;
        } else {
            expectedCorners = 0; // fallback
        }

        // Set epsilon for RDP simplification (tweak if needed)
        float epsilon = 10f;

        boolean isCorrect = drawingView.isShapeCorrect(drawingView.getAllPoints(), expectedCorners, epsilon);
        showResultDialog(isCorrect);
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

        // Announce result for TalkBack users
        announce(message);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                generateNewQuestion();
            }
        }, 4000);
    }

    private void announce(String message) {
        if (accessibilityManager.isEnabled()) {
            binding.getRoot().announceForAccessibility(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
