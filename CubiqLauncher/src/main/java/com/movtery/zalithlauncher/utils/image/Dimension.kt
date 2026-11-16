package com.movtery.zalithlauncher.utils.image

import kotlin.math.min

class Dimension(@JvmField var width: Int, @JvmField var height: Int) {
    companion object {
        @JvmStatic
        fun resizeWithRatio(imageWidth: Int, imageHeight: Int, maxSize: Int): Dimension {
            if (imageWidth <= 0 || imageHeight <= 0) return Dimension(maxSize, maxSize)
            val widthRatio = maxSize.toDouble() / imageWidth
            val heightRatio = maxSize.toDouble() / imageHeight
            val ratio = min(widthRatio, heightRatio)
            return Dimension((imageWidth * ratio).toInt(), (imageHeight * ratio).toInt())
        }
    }
}

