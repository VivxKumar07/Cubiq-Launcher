package com.movtery.zalithlauncher.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDisplayLayoutTest {
    @Test
    fun mapsLetterboxedDisplayCoordinatesToRenderCoordinates() {
        val layout = computeGameDisplayLayout(
            screenSize = IntSize(2400, 1080),
            renderSize = IntSize(1280, 720)
        )

        assertEquals(IntSize(1920, 1080), layout.displaySize)
        assertEquals(IntOffset(240, 0), layout.offset)
        assertEquals(Offset.Zero, layout.mapToGame(Offset(240f, 0f)))
        assertEquals(Offset(640f, 360f), layout.mapToGame(Offset(1200f, 540f)))
        assertEquals(Offset(1280f, 720f), layout.mapToGame(Offset(2160f, 1080f)))
    }

    @Test
    fun mapsPortraitCoordinatesWithIndependentRenderScaling() {
        val layout = computeGameDisplayLayout(
            screenSize = IntSize(1080, 2400),
            renderSize = IntSize(1600, 900)
        )

        assertEquals(IntSize(1080, 608), layout.displaySize)
        assertEquals(IntOffset(0, 896), layout.offset)
        assertEquals(Offset(800f, 450f), layout.mapToGame(Offset(540f, 1200f)))
    }
}
