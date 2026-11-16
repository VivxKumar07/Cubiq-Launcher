package net.kdt.pojavlaunch.customcontrols.mouse;

import static org.lwjgl.glfw.CallbackBridge.sendMouseButton;

import android.os.Handler;

import com.movtery.zalithlauncher.setting.AllStaticSettings;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.MathUtils;

import org.lwjgl.glfw.CallbackBridge;

public class LeftClickGesture extends ValidatorGesture {
    public static final int FINGER_STILL_THRESHOLD = (int) Tools.dpToPx(9);
    private float mGestureStartX, mGestureStartY, mGestureEndX, mGestureEndY;
    private boolean mMouseActivated;

    public LeftClickGesture(Handler handler) {
        super(handler);
    }

    public final void inputEvent() {
        if (submit()) {
            mGestureStartX = mGestureEndX = CallbackBridge.mouseX;
            mGestureStartY = mGestureEndY = CallbackBridge.mouseY;
        }
    }

    @Override
    protected int getGestureDelay() {
        return AllStaticSettings.timeLongPressTrigger;
    }

    @Override
    public boolean checkAndTrigger() {
        boolean fingerStill = LeftClickGesture.isFingerStill(mGestureStartX, mGestureStartY, mGestureEndX, mGestureEndY, FINGER_STILL_THRESHOLD);
        if (fingerStill) {
            sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT, true);
            mMouseActivated = true;
        }
        return true;
    }

    @Override
    public void onGestureCancelled(boolean isSwitching) {
        if (mMouseActivated) {
            sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT, false);
            mMouseActivated = false;
        }
    }

    public void setMotion(float deltaX, float deltaY) {
        mGestureEndX += deltaX;
        mGestureEndY += deltaY;
    }

    public static boolean isFingerStill(float startX, float startY, float threshold) {
        return MathUtils.dist(CallbackBridge.mouseX, CallbackBridge.mouseY, startX, startY) <= threshold;
    }

    public static boolean isFingerStill(float startX, float startY, float endX, float endY, float threshold) {
        return MathUtils.dist(endX, endY, startX, startY) <= threshold;
    }
}

