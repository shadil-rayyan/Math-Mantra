package com.zendalona.mathsmantra.utility;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerUtility implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 750.0f; // Adjusted based on real-world tests
    private static final int SHAKE_COOLDOWN = 500; // 500ms cooldown before detecting next shake

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isDeviceShaken = false;
    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private long lastShakeTime = 0;

    public AccelerometerUtility(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    // Explicitly call this method to start listening
    public void registerListener() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void unregisterListener() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastUpdate) > 100) { // Limit frequency
                long diffTime = (currentTime - lastUpdate);
                lastUpdate = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD && (currentTime - lastShakeTime > SHAKE_COOLDOWN)) {
                    isDeviceShaken = true;
                    lastShakeTime = currentTime;
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No need to handle accuracy changes
    }

    public boolean isDeviceShaken() {
        if (isDeviceShaken) {
            isDeviceShaken = false; // Reset the flag only after reading it once
            return true;
        }
        return false;
    }
}
