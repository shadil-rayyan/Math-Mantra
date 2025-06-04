package com.zendalona.mathsmantra.model;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationSensorUtility implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private RotationListener listener;

    public interface RotationListener {
        void onRotationChanged(float azimuth, float pitch, float roll);
    }

    public RotationSensorUtility(Context context, RotationListener listener) {
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (rotationSensor != null) {
                sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    public void unregisterListener() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            float azimuth = (float) Math.toDegrees(orientationValues[0]); // Rotation around Z-axis
            float pitch = (float) Math.toDegrees(orientationValues[1]);   // Rotation around X-axis
            float roll = (float) Math.toDegrees(orientationValues[2]);    // Rotation around Y-axis

            if (listener != null) {
                listener.onRotationChanged(azimuth, pitch, roll);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No need to handle accuracy changes
    }
}
