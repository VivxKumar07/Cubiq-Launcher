package com.movtery.zalithlauncher.game.renderer

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class RendererCompatibilityTest {
    @Test
    fun ltwRejectsVersionsBelowItsSupportedMinimum() {
        assertFalse(RendererType.LTW.isCompatibleWithVersion("1.8.9 Forge"))
        assertTrue(RendererType.LTW.isCompatibleWithVersion("1.17"))
        assertTrue(RendererType.LTW.isCompatibleWithVersion("1.20.1"))
    }

    @Test
    fun legacyAndDefaultRenderersSupportOlderMinecraft() {
        assertTrue(RendererType.GL4ES.isCompatibleWithVersion("1.8.9 Forge"))
        assertTrue(RendererType.GL4ES.isCompatibleWithVersion("1.16.5"))
        assertFalse(RendererType.GL4ES.isCompatibleWithVersion("1.17"))
        assertFalse(RendererType.KRYPTON.isCompatibleWithVersion("1.8.9 Forge"))
        assertTrue(RendererType.KRYPTON.isCompatibleWithVersion("1.17"))
    }

    @Test
    fun virglHonorsItsSupportedVersionRange() {
        assertTrue(RendererType.VIRGL.isCompatibleWithVersion("1.12.2"))
        assertTrue(RendererType.VIRGL.isCompatibleWithVersion("1.16.5"))
        assertFalse(RendererType.VIRGL.isCompatibleWithVersion("1.17"))
    }
}
