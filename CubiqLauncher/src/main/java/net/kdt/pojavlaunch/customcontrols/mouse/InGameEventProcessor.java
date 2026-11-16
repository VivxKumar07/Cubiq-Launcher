package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import com.movtery.zalithlauncher.game.support.touch_controller.ContactHandler;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;

import org.lwjgl.glfw.CallbackBridge;

public class InGameEventProcessor implements TouchEventProcessor {
    private final Handler mGestureHandler = new Handler(Looper.getMainLooper());
    private final double mSensitivity;
    private boolean mEventTransitioned = true;
    private final PointerTracker mTracker = new PointerTracker();
    private final LeftClickGesture mLeftClickGesture = new LeftClickGesture(mGestureHandler);
    private final RightClickGesture mRightClickGesture = new RightClickGesture(mGestureHandler);

    public InGameEventProcessor(double sensitivity) {
        mSensitivity = sensitivity;
    }

    @Override
    public boolean processTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTracker.startTracking(motionEvent);
                if (AllSettings.getDisableGestures().getValue()) break;
                mEventTransitioned = false;
                checkGestures();
                break;
            case MotionEvent.ACTION_MOVE:
                mTracker.trackEvent(motionEvent);
                float[] motionVector = mTracker.getMotionVector();
                float deltaX = (float) (motionVector[0] * mSensitivity);
                float deltaY = (float) (motionVector[1] * mSensitivity);
                mLeftClickGesture.setMotion(deltaX, deltaY);
                mRightClickGesture.setMotion(deltaX, deltaY);
                CallbackBridge.sendCursorDelta(deltaX, deltaY);
                if (AllSettings.getDisableGestures().getValue()) break;
                checkGestures();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTracker.cancelTracking();
                cancelGestures(false);
        }
        return true;
    }

    @Override
    public void cancelPendingActions() {
        cancelGestures(true);
    }

    @Override
    public void dispatchTouchEvent(MotionEvent event, View view) {
        if (AllStaticSettings.useControllerProxy) {
            ContactHandler.INSTANCE.progressEvent(event, view);
        }
    }

    private void checkGestures() {
        mLeftClickGesture.inputEvent();
        if (!mEventTransitioned) mRightClickGesture.inputEvent();
    }

    private void cancelGestures(boolean isSwitching) {
        mEventTransitioned = true;
        mLeftClickGesture.cancel(isSwitching);
        mRightClickGesture.cancel(isSwitching);
    }
}

