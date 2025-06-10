package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences;
import com.zendalona.mathsmantra.utility.settings.LocaleHelper;
import com.zendalona.mathsmantra.view.DrawingView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrawingFragment extends Fragment {

    private FragmentGameDrawingBinding binding;
    private DrawingView drawingView;
    private AccessibilityManager accessibilityManager;

    private List<String> shapeList = new ArrayList<>();
    private int currentIndex = 0;
    private String lang = "en";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameDrawingBinding.inflate(inflater, container, false);
        Context context = requireContext();

        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        // Add the custom drawing view to the container
        drawingView = new DrawingView(context);
        binding.drawingContainer.addView(drawingView);

        // Load language and difficulty
        String difficulty = DifficultyPreferences.getDifficulty(context);
        lang = LocaleHelper.getLanguage(context);
        if (TextUtils.isEmpty(lang)) lang = "en";

        shapeList = loadShapesFromAssets(lang, difficulty);

        setupListeners();
        loadNextShape();

        return binding.getRoot();
    }

    private List<String> loadShapesFromAssets(String lang, String difficulty) {
        List<String> shapes = new ArrayList<>();
        String fileName = lang + "/game/drawing/" + difficulty.toLowerCase(Locale.ROOT) + ".txt";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open(fileName)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    shapes.add(line.trim());
                }
            }

            if (shapes.isEmpty()) {
                showToast("No shapes found in " + fileName);
            }

        } catch (IOException e) {
            showToast("Failed to load shapes from: " + fileName);
            e.printStackTrace();
        }

        return shapes;
    }

    private void setupListeners() {
        binding.resetButton.setOnClickListener(v -> {
            drawingView.clearCanvas();
            announce(getString(R.string.canvas_cleared));
        });

        binding.submitButton.setOnClickListener(v -> {
            showResultDialogAndNext(); // No checking, just move ahead
        });
    }

    private void loadNextShape() {
        if (currentIndex >= shapeList.size()) {
            announce(getString(R.string.task_completed));
            new Handler(Looper.getMainLooper()).postDelayed(() -> requireActivity().onBackPressed(), 3000);
            return;
        }

        String shape = shapeList.get(currentIndex);
        String instruction = getString(R.string.drawing_task, shape);

        binding.questionText.setText(instruction);
        binding.questionText.setContentDescription(instruction);
        binding.questionText.announceForAccessibility(instruction);
        drawingView.clearCanvas();
    }

    private void showResultDialogAndNext() {
        // Generic success feedback
        String message = getString(R.string.right_answer);
        int gifResource = R.drawable.right;

        DialogResultBinding dialogBinding = DialogResultBinding.inflate(getLayoutInflater());
        View dialogView = dialogBinding.getRoot();

        Glide.with(this).asGif().load(gifResource).into(dialogBinding.gifImageView);
        dialogBinding.messageTextView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        announce(message);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            currentIndex++;
            loadNextShape();
        }, 3000);
    }

    private void announce(String message) {
        if (accessibilityManager.isEnabled()) {
            binding.getRoot().announceForAccessibility(message);
        }
    }

    private void showToast(String msg) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
