package com.movtery.zalithlauncher.setting

/**
 * Static runtime cache for ZL1 in-game controls and touch/mouse/gyro/hotbar pipelines.
 * Keeps original ZL1 AllStaticSettings semantics while reading values from Cubiq's MMKV AllSettings.
 */
class AllStaticSettings {
    companion object {
        @JvmField
        var scaleFactor: Float = AllSettings.resolutionRatio.getValue() / 100f

        @JvmField
        var disableDoubleTap: Boolean = AllSettings.disableDoubleTap.getValue()

        @JvmField
        var timeLongPressTrigger: Int = AllSettings.timeLongPressTrigger.getValue()

        @JvmField
        var useControllerProxy: Boolean = false

        @JvmField
        var enableGyro: Boolean = AllSettings.gyroscopeControl.getValue()

        @JvmField
        var gyroSensitivity: Float = AllSettings.gyroscopeSensitivity.getValue().toFloat()

        @JvmField
        var gyroSampleRate: Int = AllSettings.gyroscopeSampleRate.getValue()

        @JvmField
        var gyroSmoothing: Boolean = AllSettings.gyroscopeSmoothing.getValue()

        @JvmField
        var gyroInvertX: Boolean = AllSettings.gyroscopeInvertX.getValue()

        @JvmField
        var gyroInvertY: Boolean = AllSettings.gyroscopeInvertY.getValue()

        @JvmStatic
        fun refresh(useControllerProxy: Boolean = this.useControllerProxy) {
            AllSettings.reloadAll()
            scaleFactor = AllSettings.resolutionRatio.getValue() / 100f
            disableDoubleTap = AllSettings.disableDoubleTap.getValue()
            timeLongPressTrigger = AllSettings.timeLongPressTrigger.getValue()
            this.useControllerProxy = useControllerProxy
            enableGyro = AllSettings.gyroscopeControl.getValue()
            gyroSensitivity = AllSettings.gyroscopeSensitivity.getValue().toFloat()
            gyroSampleRate = AllSettings.gyroscopeSampleRate.getValue()
            gyroSmoothing = AllSettings.gyroscopeSmoothing.getValue()
            gyroInvertX = AllSettings.gyroscopeInvertX.getValue()
            gyroInvertY = AllSettings.gyroscopeInvertY.getValue()
        }
    }
}

