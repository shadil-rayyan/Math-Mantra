package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.model.RotationSensorUtility;

import java.util.Random;

public class AngleFragment extends Fragment implements RotationSensorUtility.RotationListener {

    private TextView rotationTextView, questionTextView;
    private RotationSensorUtility rotationSensorUtility;
    private float targetRotation = 0f;
    private float baseAzimuth = -1f;  // initial compass direction
    private boolean questionAnswered = false;
    private Handler angleUpdateHandler;
    private Runnable angleUpdateRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_angle, container, false);

        rotationTextView = view.findViewById(R.id.rotation_angle_text);
        questionTextView = view.findViewById(R.id.angle_question);

        rotationSensorUtility = new RotationSensorUtility(requireContext(), this);

        angleUpdateHandler = new Handler(Looper.getMainLooper());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rotationSensorUtility != null) rotationSensorUtility.unregisterListener();
        if (angleUpdateHandler != null) angleUpdateHandler.removeCallbacks(angleUpdateRunnable);
    }

    @Override
    public void onRotationChanged(float azimuth, float pitch, float roll) {
        if (baseAzimuth < 0) {
            baseAzimuth = azimuth;  // Set first reading as the base direction
            generateNewQuestion();
            return;
        }

        float relativeAzimuth = (azimuth - baseAzimuth + 360) % 360;

        if (rotationTextView != null && isAdded()) {
            requireActivity().runOnUiThread(() -> {
                rotationTextView.setText(String.format("Relative Angle: %d°", (int) relativeAzimuth));
                checkIfCorrect(relativeAzimuth);
            });
        }
    }

    private void checkIfCorrect(float currentAngle) {
        if (questionAnswered) return;

        boolean isCorrect = Math.abs(targetRotation - currentAngle) <= 10;

        if (isCorrect) {
            questionAnswered = true;
            angleUpdateHandler.removeCallbacks(angleUpdateRunnable);
            showResultDialog(true);
        }
    }

    private void showResultDialog(boolean isCorrect) {
        String message = isCorrect ? "Right Answer!" : "Wrong Answer!";
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
        dialogView.announceForAccessibility(message);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                generateNewQuestion();
            }
        }, 4000);
    }

    private void generateNewQuestion() {
        int[] validAngles = {45, 90, 120, 180, 270};
        targetRotation = validAngles[new Random().nextInt(validAngles.length)];
        questionAnswered = false;

        String questionText = "Turn to " + (int) targetRotation + "° from your current direction";
        questionTextView.setText(questionText);
        questionTextView.announceForAccessibility(questionText);

        if (angleUpdateRunnable == null) {
            angleUpdateRunnable = () -> {
                if (!questionAnswered && rotationTextView != null) {
                    rotationTextView.announceForAccessibility("Current Angle: " + rotationTextView.getText());
                    angleUpdateHandler.postDelayed(angleUpdateRunnable, 2000);
                }
            };
        }
        angleUpdateHandler.postDelayed(angleUpdateRunnable, 2000);
    }
}
