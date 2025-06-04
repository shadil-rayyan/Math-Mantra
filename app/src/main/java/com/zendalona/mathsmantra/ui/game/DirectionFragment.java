package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zendalona.mathmantra.databinding.FragmentDirectionBinding;
import com.zendalona.mathmantra.utils.RandomValueGenerator;

public class DirectionFragment extends Fragment implements SensorEventListener {

    private FragmentDirectionBinding binding;
    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private RandomValueGenerator random;

    private final float[] lastAccelerometer = new float[3];
    private final float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private float targetDirection = 0f;
    private boolean questionAnswered = false;

    private final String[] compassDirections = {
            "North", "North-Northeast", "Northeast", "East-Northeast",
            "East", "East-Southeast", "Southeast", "South-Southeast",
            "South", "South-Southwest", "Southwest", "West-Southwest",
            "West", "West-Northwest", "Northwest", "North-Northwest"
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        random = new RandomValueGenerator();

        if (sensorManager != null) {
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDirectionBinding.inflate(inflater, container, false);
        generateNewQuestion();  // Initial question
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuthInRadians = orientation[0];
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            updateCompassUI(-azimuthInDegrees);
        }
    }

    private void updateCompassUI(float rotationDegrees) {
        if (binding == null) return;

        binding.compass.setRotation(rotationDegrees);

        String directionText = getCompassDirection((rotationDegrees + 360) % 360);
        binding.degreeText.setText(directionText);

        // Set accessible content description
        binding.degreeText.setContentDescription("Current direction: " + directionText);

        // Announce the real-time direction for TalkBack users
        binding.degreeText.announceForAccessibility("Current direction: " + directionText);

        checkIfCorrect(rotationDegrees);
    }

    private void checkIfCorrect(float currentDegrees) {
        if (questionAnswered) return;

        float difference = Math.abs(targetDirection - ((currentDegrees + 360) % 360));

        if (difference <= 22.5) { // 22.5 degrees tolerance to match compass points
            questionAnswered = true;
            showResultDialog();
        }
    }

    private void showResultDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Correct!")
                .setMessage("You turned to the correct direction!")
                .setCancelable(false)
                .create();

        dialog.show();

        // Announce result to TalkBack
        binding.getRoot().announceForAccessibility("Correct! You turned to the correct direction!");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                generateNewQuestion();
            }
        }, 3000);
    }

    private void generateNewQuestion() {
        targetDirection = random.generateRandomDegree();
        String targetDirectionText = getCompassDirection(targetDirection);
        questionAnswered = false;

        String questionText = "Turn to " + targetDirectionText;
        binding.questionTv.setText(questionText);

        // Announce question to TalkBack
        binding.questionTv.announceForAccessibility(questionText);
    }

    private String getCompassDirection(float degrees) {
        int index = (int) ((degrees + 11.25) / 22.5) % 16; // 16 compass points, 22.5° each
        return compassDirections[index];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No need to handle this for now
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
