package net.kdt.pojavlaunch.customcontrols.mouse;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

import android.view.MotionEvent;

import net.kdt.pojavlaunch.Tools;

public class TapDetector {
    public final static int DETECTION_METHOD_DOWN = 0x1;
    public final static int DETECTION_METHOD_UP = 0x2;
    public final static int DETECTION_METHOD_BOTH = 0x3;

    private final static int TAP_MIN_DELTA_MS = -1;
    private final static int TAP_MAX_DELTA_MS = 300;
    private final static int TAP_SLOP_SQUARE_PX = (int) Tools.dpToPx(2500);

    private final int mTapNumberToDetect;
    private int mCurrentTapNumber = 0;

    private final int mDetectionMethod;

    private long mLastEventTime = 0;
    private float mLastX = 9999;
    private float mLastY = 9999;

    public TapDetector(int tapNumberToDetect, int detectionMethod) {
        this.mDetectionMethod = detectionMethod;
        this.mTapNumberToDetect = detectBothTouch() ? 2 * tapNumberToDetect : tapNumberToDetect;
    }

    public boolean onTouchEvent(MotionEvent e) {
        int eventAction = e.getActionMasked();
        int pointerIndex = -1;

        if (detectDownTouch()) {
            if (eventAction == ACTION_DOWN) pointerIndex = 0;
            else if (eventAction == ACTION_POINTER_DOWN) pointerIndex = e.getActionIndex();
        }
        if (detectUpTouch()) {
            if (eventAction == ACTION_UP) pointerIndex = 0;
            else if (eventAction == ACTION_POINTER_UP) pointerIndex = e.getActionIndex();
        }

        if (pointerIndex == -1) return false;

        float eventX = e.getX(pointerIndex);
        float eventY = e.getY(pointerIndex);
        long eventTime = e.getEventTime();

        long deltaTime = eventTime - mLastEventTime;
        int deltaX = (int) mLastX - (int) eventX;
        int deltaY = (int) mLastY - (int) eventY;

        mLastEventTime = eventTime;
        mLastX = eventX;
        mLastY = eventY;

        if (mCurrentTapNumber > 0) {
            if ((deltaTime < TAP_MIN_DELTA_MS || deltaTime > TAP_MAX_DELTA_MS) ||
                    ((deltaX * deltaX + deltaY * deltaY) > TAP_SLOP_SQUARE_PX)) {
                if (mDetectionMethod == DETECTION_METHOD_BOTH && (eventAction == ACTION_UP || eventAction == ACTION_POINTER_UP)) {
                    resetTapDetectionState();
                    return false;
                } else {
                    mCurrentTapNumber = 0;
                }
            }
        }

        mCurrentTapNumber += 1;
        if (mCurrentTapNumber >= mTapNumberToDetect) {
            resetTapDetectionState();
            return true;
        }
        return false;
    }

    private void resetTapDetectionState() {
        mCurrentTapNumber = 0;
        mLastEventTime = 0;
        mLastX = 9999;
        mLastY = 9999;
    }

    private boolean detectDownTouch() {
        return (mDetectionMethod & DETECTION_METHOD_DOWN) == DETECTION_METHOD_DOWN;
    }

    private boolean detectUpTouch() {
        return (mDetectionMethod & DETECTION_METHOD_UP) == DETECTION_METHOD_UP;
    }

    private boolean detectBothTouch() {
        return mDetectionMethod == DETECTION_METHOD_BOTH;
    }
}

