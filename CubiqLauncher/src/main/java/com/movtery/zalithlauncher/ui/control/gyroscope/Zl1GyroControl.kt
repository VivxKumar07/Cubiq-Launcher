package com.movtery.zalithlauncher.ui.control.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import com.movtery.zalithlauncher.setting.AllSettings
import net.kdt.pojavlaunch.GrabListener
import org.lwjgl.glfw.CallbackBridge
import java.util.Arrays
import kotlin.math.abs

class Zl1GyroControl(context: Context) : SensorEventListener, GrabListener {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val correctionListener = OrientationCorrectionListener(context)
    private val angleBuffer = Array(
        if (AllSettings.gyroscopeSmoothing.getValue()) 2 else 1
    ) { FloatArray(3) }
    private val previousRotation = FloatArray(16)
    private val currentRotation = FloatArray(16)
    private val angleDifference = FloatArray(3)

    private var surfaceRotation = -10
    private var shouldHandleEvents = false
    private var warmup = 0
    private var xFactor = 1f
    private var yFactor = 1f
    private var baseXFactor = 1f
    private var baseYFactor = 1f
    private var swapXY = false
    private var xTotal = 0f
    private var yTotal = 0f
    private var xAverage = 0f
    private var yAverage = 0f
    private var historyIndex = -1
    private var storedX = 0f
    private var storedY = 0f

    init {
        updateOrientation()
    }

    fun enable() {
        val gyroSensor = sensor ?: return
        warmup = ROTATION_VECTOR_WARMUP_PERIOD
        sensorManager.registerListener(
            this,
            gyroSensor,
            1_000 * AllSettings.gyroscopeSampleRate.getValue()
        )
        correctionListener.enable()
        shouldHandleEvents = CallbackBridge.isGrabbing()
        CallbackBridge.addGrabListener(this)
    }

    fun disable() {
        if (sensor == null) return
        sensorManager.unregisterListener(this)
        correctionListener.disable()
        storedX = 0f
        storedY = 0f
        resetDamper()
        CallbackBridge.removeGrabListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!shouldHandleEvents) return

        System.arraycopy(currentRotation, 0, previousRotation, 0, currentRotation.size)
        SensorManager.getRotationMatrixFromVector(currentRotation, event.values)

        if (warmup > 0) {
            warmup--
            return
        }

        SensorManager.getAngleChange(angleDifference, currentRotation, previousRotation)
        damperValue(angleDifference)
        val sensitivity = AllSettings.gyroscopeSensitivity.getValue()
        storedX += xAverage * 10 * sensitivity
        storedY += yAverage * 10 * sensitivity

        val absX = abs(storedX)
        val absY = abs(storedY)
        var updatePosition = false
        if (absX + absY > MULTI_AXIS_LOW_PASS_THRESHOLD) {
            CallbackBridge.mouseX -= (if (swapXY) storedY else storedX) * xFactor
            CallbackBridge.mouseY += (if (swapXY) storedX else storedY) * yFactor
            storedX = 0f
            storedY = 0f
            updatePosition = true
        } else {
            if (absX > SINGLE_AXIS_LOW_PASS_THRESHOLD) {
                CallbackBridge.mouseX -= (if (swapXY) storedY else storedX) * xFactor
                storedX = 0f
                updatePosition = true
            }
            if (absY > SINGLE_AXIS_LOW_PASS_THRESHOLD) {
                CallbackBridge.mouseY += (if (swapXY) storedX else storedY) * yFactor
                storedY = 0f
                updatePosition = true
            }
        }

        if (updatePosition) {
            CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY)
        }
    }

    @Suppress("DEPRECATION")
    fun updateOrientation() {
        surfaceRotation = windowManager.defaultDisplay.rotation
        when (surfaceRotation) {
            Surface.ROTATION_0 -> {
                swapXY = true
                baseXFactor = 1f
                baseYFactor = 1f
            }
            Surface.ROTATION_90 -> {
                swapXY = false
                baseXFactor = -1f
                baseYFactor = 1f
            }
            Surface.ROTATION_180 -> {
                swapXY = true
                baseXFactor = -1f
                baseYFactor = -1f
            }
            Surface.ROTATION_270 -> {
                swapXY = false
                baseXFactor = 1f
                baseYFactor = -1f
            }
        }
        applyInversion()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onGrabState(isGrabbing: Boolean) {
        warmup = ROTATION_VECTOR_WARMUP_PERIOD
        shouldHandleEvents = isGrabbing
    }

    private fun damperValue(newAngleDifference: FloatArray) {
        historyIndex++
        if (historyIndex >= angleBuffer.size) historyIndex = 0

        xTotal -= angleBuffer[historyIndex][1]
        yTotal -= angleBuffer[historyIndex][2]
        System.arraycopy(newAngleDifference, 0, angleBuffer[historyIndex], 0, 3)
        xTotal += angleBuffer[historyIndex][1]
        yTotal += angleBuffer[historyIndex][2]
        xAverage = xTotal / angleBuffer.size
        yAverage = yTotal / angleBuffer.size
    }

    private fun resetDamper() {
        historyIndex = -1
        xTotal = 0f
        yTotal = 0f
        xAverage = 0f
        yAverage = 0f
        angleBuffer.forEach { Arrays.fill(it, 0f) }
    }

    private fun applyInversion() {
        xFactor = baseXFactor * if (AllSettings.gyroscopeInvertX.getValue()) -1 else 1
        yFactor = baseYFactor * if (AllSettings.gyroscopeInvertY.getValue()) -1 else 1
    }

    private inner class OrientationCorrectionListener(context: Context) :
        OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
        override fun onOrientationChanged(orientation: Int) {
            if (!shouldHandleEvents || orientation == ORIENTATION_UNKNOWN) return

            when (surfaceRotation) {
                Surface.ROTATION_90, Surface.ROTATION_270 -> {
                    swapXY = false
                    if (orientation in 226..314) {
                        baseXFactor = -1f
                        baseYFactor = 1f
                    } else if (orientation in 46..134) {
                        baseXFactor = 1f
                        baseYFactor = -1f
                    }
                }
                Surface.ROTATION_0, Surface.ROTATION_180 -> {
                    swapXY = true
                    if (orientation >= 316 || orientation < 45) {
                        baseXFactor = 1f
                        baseYFactor = 1f
                    } else if (orientation in 136..224) {
                        baseXFactor = -1f
                        baseYFactor = -1f
                    }
                }
            }
            applyInversion()
        }
    }

    private companion object {
        const val SINGLE_AXIS_LOW_PASS_THRESHOLD = 1.13f
        const val MULTI_AXIS_LOW_PASS_THRESHOLD = 1.3f
        const val ROTATION_VECTOR_WARMUP_PERIOD = 2
    }
}
