package com.movtery.zalithlauncher.game.support.touch_controller

import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient

/**
 * Bridges View-based ZL1 MotionEvents to Cubiq's TouchController LauncherProxyClient.
 */
object ContactHandler {
    private val pointerIdMap = SparseIntArray()
    private var nextPointerId = 1

    fun progressEvent(event: MotionEvent, view: View) {
        val client = ControllerProxy.proxyClient.value ?: return
        val width = view.width.takeIf { it > 0 } ?: return
        val height = view.height.takeIf { it > 0 } ?: return

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handlePointerDown(event, client, 0, width, height)

            MotionEvent.ACTION_POINTER_DOWN -> handlePointerDown(event, client, event.actionIndex, width, height)

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = pointerIdMap.get(event.getPointerId(i))
                    if (pointerId != 0) {
                        client.addPointer(pointerId, event.getX(i) / width, event.getY(i) / height)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                client.clearPointer()
                pointerIdMap.clear()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val i = event.actionIndex
                val androidId = event.getPointerId(i)
                val pointerId = pointerIdMap.get(androidId)
                if (pointerId != 0) {
                    pointerIdMap.delete(androidId)
                    client.removePointer(pointerId)
                }
            }
        }
    }

    private fun handlePointerDown(
        event: MotionEvent,
        client: LauncherProxyClient,
        index: Int,
        width: Int,
        height: Int
    ) {
        val pointerId = nextPointerId++
        pointerIdMap.put(event.getPointerId(index), pointerId)
        client.addPointer(pointerId, event.getX(index) / width, event.getY(index) / height)
    }
}

