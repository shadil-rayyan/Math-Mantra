package com.zendalona.zmantra.utility.game.angle

import android.content.Context
import android.hardware.*

class RotationSensorUtility(
    context: Context,
    private val listener: RotationListener
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    interface RotationListener {
        fun onRotationChanged(azimuth: Float, pitch: Float, roll: Float)
    }

    fun registerListener() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientationValues = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationValues)

            val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()

            listener.onRotationChanged(azimuth, pitch, roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
