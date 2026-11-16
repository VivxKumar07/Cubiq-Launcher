package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;

import org.lwjgl.glfw.CallbackBridge;

public class RightClickGesture extends ValidatorGesture {
    private boolean mGestureEnabled = true;
    private boolean mGestureValid = true;
    private float mGestureStartX, mGestureStartY, mGestureEndX, mGestureEndY;

    public RightClickGesture(Handler mHandler) {
        super(mHandler);
    }

    public final void inputEvent() {
        if (!mGestureEnabled) return;
        if (submit()) {
            mGestureStartX = mGestureEndX = CallbackBridge.mouseX;
            mGestureStartY = mGestureEndY = CallbackBridge.mouseY;
            mGestureEnabled = false;
            mGestureValid = true;
        }
    }

    public void setMotion(float deltaX, float deltaY) {
        mGestureEndX += deltaX;
        mGestureEndY += deltaY;
    }

    @Override
    protected int getGestureDelay() {
        return 150;
    }

    @Override
    public boolean checkAndTrigger() {
        mGestureValid = false;
        return true;
    }

    @Override
    public void onGestureCancelled(boolean isSwitching) {
        mGestureEnabled = true;
        if (!mGestureValid || isSwitching) return;
        boolean fingerStill = LeftClickGesture.isFingerStill(mGestureStartX, mGestureStartY, mGestureEndX, mGestureEndY, LeftClickGesture.FINGER_STILL_THRESHOLD);
        if (!fingerStill) return;
        CallbackBridge.sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, true);
        CallbackBridge.sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, false);
    }
}

